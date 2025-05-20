package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _112_WalkOfFate extends Quest implements ScriptFile
{
	// npc
	private static final int seer_livina = 30572;
	private static final int karuda = 32017;

	// 
	private static final int scrl_of_ench_am_d = 956;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _112_WalkOfFate()
	{
		super(false);
		addStartNpc(seer_livina);
		addTalkId(karuda);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("step_of_destiny");
		int npcId = npc.getNpcId();

		if(npcId == seer_livina)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("step_of_destiny", String.valueOf(1 * 10 + 1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "seer_livina_q0112_0104.htm";
			}
		}
		else if(npcId == karuda)
			if(event.equalsIgnoreCase("reply_3"))
				if(GetMemoState >= (2 - 1) * 10 + 1)
				{
					st.giveItems(ADENA_ID, 22308);
					st.addExpAndSp(112876, 5774);
					st.giveItems(scrl_of_ench_am_d, 1);
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(false);
					htmltext = "karuda_q0112_0201.htm";
				}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("step_of_destiny");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == seer_livina)
					if(st.getPlayer().getLevel() >= 20)
						htmltext = "seer_livina_q0112_0101.htm";
					else
					{
						st.exitQuest(true);
						htmltext = "seer_livina_q0112_0103.htm";
					}
				break;
			case STARTED:
				if(npcId == seer_livina)
				{
					if(GetMemoState == 1 * 10 + 1)
						htmltext = "seer_livina_q0112_0105.htm";
				}
				else if(npcId == karuda)
					if(GetMemoState == 1 * 10 + 1)
						htmltext = "karuda_q0112_0101.htm";
				break;
		}
		return htmltext;
	}
}
