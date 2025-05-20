package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _303_CollectArrowheads extends Quest implements ScriptFile
{
	int ORCISH_ARROWHEAD = 963;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _303_CollectArrowheads()
	{
		super(false);

		addStartNpc(30029);

		addTalkId(30029);

		addKillId(20361);

		addQuestItem(ORCISH_ARROWHEAD);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("minx_q0303_04.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();

		if(cond == 0)
			if(st.getPlayer().getLevel() >= 10)
				htmltext = "minx_q0303_03.htm";
			else
			{
				htmltext = "minx_q0303_02.htm";
				st.exitQuest(true);
			}
		else if(st.ownItemCount(ORCISH_ARROWHEAD) < 10)
			htmltext = "minx_q0303_05.htm";
		else
		{
			st.takeItems(ORCISH_ARROWHEAD, -1);
			st.giveItems(ADENA_ID, 1000);
			st.addExpAndSp(2000, 0);
			htmltext = "minx_q0303_06.htm";
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.ownItemCount(ORCISH_ARROWHEAD) < 10)
		{
			st.giveItems(ORCISH_ARROWHEAD, 1);
			if(st.ownItemCount(ORCISH_ARROWHEAD) == 10)
			{
				st.setCond(2);
				st.soundEffect(SOUND_MIDDLE);
			}
			else
				st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}