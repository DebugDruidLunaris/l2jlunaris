package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _121_PavelTheGiants extends Quest implements ScriptFile
{
	//NPCs
	private static int NEWYEAR = 31961;
	private static int YUMI = 32041;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _121_PavelTheGiants()
	{
		super(false);

		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR, YUMI);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equals("collecter_yumi_q0121_0201.htm"))
		{
			st.soundEffect(SOUND_FINISH);
			st.addExpAndSp(346320, 26069);
			st.exitQuest(false);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();

		if(id == CREATED && npcId == NEWYEAR)
		{
			if(st.getPlayer().getLevel() >= 70)
			{
				htmltext = "head_blacksmith_newyear_q0121_0101.htm";
				st.setCond(1);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
			}
			else
			{
				htmltext = "head_blacksmith_newyear_q0121_0103.htm";
				st.exitQuest(false);
			}
		}
		else if(id == STARTED)
			if(npcId == YUMI && cond == 1)
				htmltext = "collecter_yumi_q0121_0101.htm";
			else
				htmltext = "head_blacksmith_newyear_q0121_0105.htm";
		return htmltext;
	}
}