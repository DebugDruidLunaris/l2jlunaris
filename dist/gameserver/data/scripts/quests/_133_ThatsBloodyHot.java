package quests;

import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _133_ThatsBloodyHot extends Quest implements ScriptFile
{
	// npc
	private static int priest_kanis = 32264;
	private static int galate = 32292;

	// questitem
	private static int q_pure_crystal_sample = 9785;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _133_ThatsBloodyHot()
	{
		super(false);
		addStartNpc(priest_kanis);
		addTalkId(priest_kanis, galate);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("jump_on_the_hot_plate");
		int GetStep_FieldCycle = HellboundManager.getHellboundLevel();
		int npcId = npc.getNpcId();

		if(npcId == priest_kanis)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("jump_on_the_hot_plate", String.valueOf(1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "priest_kanis_q0133_04.htm";
			}

			else if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 1)
					htmltext = "priest_kanis_q0133_06.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				if(GetMemoState == 1)
					htmltext = "priest_kanis_q0133_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
			{
				if(GetMemoState == 1)
				{
					st.setMemoState("jump_on_the_hot_plate", String.valueOf(2), true);
					htmltext = "priest_kanis_q0133_08.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				if(GetMemoState == 2)
					htmltext = "priest_kanis_q0133_10.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
			{
				if(GetMemoState == 2)
					htmltext = "priest_kanis_q0133_11.htm";
			}
			else if(event.equalsIgnoreCase("reply_6"))
				if(GetMemoState == 2)
				{
					st.setCond(2);
					st.setMemoState("jump_on_the_hot_plate", String.valueOf(3), true);
					st.giveItems(q_pure_crystal_sample, 1);
					htmltext = "priest_kanis_q0133_12.htm";
				}
		}
		else if(npcId == galate)
			if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 3)
					htmltext = "Galate_q0133_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				if(GetMemoState == 3)
				{
					st.setMemoState("jump_on_the_hot_plate", String.valueOf(4), true);
					st.takeItems(q_pure_crystal_sample, -1);
					htmltext = "Galate_q0133_05.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_3"))
				if(GetMemoState == 4 && GetStep_FieldCycle >= 1)
				{
					st.giveItems(ADENA_ID, 254247);
					st.addExpAndSp(331457, 32524);
					st.removeMemo("jump_on_the_hot_plate");
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(false);
					htmltext = "Galate_q0133_06.htm";
				}
				else if(GetMemoState == 4 && GetStep_FieldCycle == 0)
				{
					st.giveItems(ADENA_ID, 254247);
					st.addExpAndSp(325881, 32524);
					st.removeMemo("jump_on_the_hot_plate");
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(false);
					htmltext = "Galate_q0133_07.htm";
				}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		QuestState qs = st.getPlayer().getQuestState(_131_BirdInACage.class);
		int GetMemoState = st.getInt("jump_on_the_hot_plate");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == priest_kanis)
					if(qs != null)
					{
						if(st.getPlayer().getLevel() >= 78 && qs.isCompleted())
							htmltext = "priest_kanis_q0133_01.htm";
						else
						{
							htmltext = "priest_kanis_q0133_02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "priest_kanis_q0133_03.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == priest_kanis)
				{
					if(GetMemoState == 1)
						htmltext = "priest_kanis_q0133_05.htm";
					else if(GetMemoState == 2)
						htmltext = "priest_kanis_q0133_09.htm";
					else if(GetMemoState >= 3)
						htmltext = "priest_kanis_q0133_13.htm";
				}
				else if(npcId == galate)
					if(GetMemoState < 3)
						htmltext = "Galate_q0133_01.htm";
					else if(GetMemoState == 3)
						htmltext = "Galate_q0133_02.htm";
					else if(GetMemoState == 4)
						htmltext = "Galate_q0133_04.htm";
				break;
		}
		return htmltext;
	}
}
