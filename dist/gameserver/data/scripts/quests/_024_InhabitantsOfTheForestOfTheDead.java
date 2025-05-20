package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _024_InhabitantsOfTheForestOfTheDead extends Quest implements ScriptFile
{
	// npc
	private final static int day_dorian = 31389;
	private final static int q_forest_stone2 = 31531;
	private final static int maid_of_ridia = 31532;
	private final static int shadow_hardin = 31522;

	// questitem
	private final static int q_letter_of_ridia = 7065;
	private final static int q_ridia_hairpin = 7148;
	private final static int q_triols_totem1 = 7151;
	private final static int q_lost_flower = 7152;
	private final static int q_silver_cross = 7153;
	private final static int q_broken_silver_cross = 7154;
	private final static int q_triols_totem2 = 7156;

	// mobs
	private final static int bone_snatcher = 21557;
	private final static int bone_snatcher_a = 21558;
	private final static int bone_shaper = 21560;
	private final static int bone_collector = 21563;
	private final static int skull_collector = 21564;
	private final static int bone_animator = 21565;
	private final static int skull_animator = 21566;
	private final static int bone_slayer = 21567;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _024_InhabitantsOfTheForestOfTheDead()
	{
		super(false);
		addStartNpc(day_dorian);
		addTalkId(q_forest_stone2, maid_of_ridia, shadow_hardin);
		addKillId(bone_snatcher, bone_snatcher_a, bone_shaper, bone_collector, skull_collector, bone_animator, skull_animator, bone_slayer);
		addQuestItem(q_triols_totem1);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("people_of_lost_forest");

		if(event.startsWith("see_creature"))
		{
			if(st.ownItemCount(q_silver_cross) > 0)
			{
				st.setCond(4);
				st.takeItems(q_silver_cross, -1);
				st.giveItems(q_broken_silver_cross, 1);
				Functions.npcSay(npc, "That sign!");
			}
			return null;
		}
		else if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			st.setMemoState("people_of_lost_forest", String.valueOf(1), true);
			st.giveItems(q_lost_flower, 1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			htmltext = "day_dorian_q0024_03.htm";
		}
		else if(event.equalsIgnoreCase("reply_1"))
		{
			st.setMemoState("people_of_lost_forest", String.valueOf(3), true);
			htmltext = "day_dorian_q0024_08.htm";
		}
		else if(event.equalsIgnoreCase("reply_3"))
		{
			if(GetMemoState == 3)
			{
				st.setCond(3);
				st.setMemoState("people_of_lost_forest", String.valueOf(4), true);
				st.giveItems(q_silver_cross, 1);
				st.soundEffect(SOUND_MIDDLE);
				htmltext = "day_dorian_q0024_13.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_5"))
		{
			st.soundEffect(SOUND_CHARSTAT_OPEN_01);
			htmltext = "day_dorian_q0024_18.htm";
		}
		else if(event.equalsIgnoreCase("reply_6"))
		{
			if(GetMemoState == 4 && st.ownItemCount(q_broken_silver_cross) >= 1)
			{
				st.setCond(5);
				st.setMemoState("people_of_lost_forest", String.valueOf(5), true);
				st.takeItems(q_broken_silver_cross, -1);
				htmltext = "day_dorian_q0024_19.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_1a"))
		{
			st.setCond(2);
			st.setMemoState("people_of_lost_forest", String.valueOf(2), true);
			st.takeItems(q_lost_flower, -1);
			st.soundEffect(SOUND_MIDDLE);
			htmltext = "q_forest_stone2_q0024_02.htm";
		}
		else if(event.equalsIgnoreCase("reply_7"))
		{
			if(GetMemoState == 5)
			{
				st.setCond(6);
				st.setMemoState("people_of_lost_forest", String.valueOf(6), true);
				st.giveItems(q_letter_of_ridia, 1);
				st.soundEffect(SOUND_MIDDLE);
				htmltext = "maid_of_ridia_q0024_04.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_8"))
		{
			if((GetMemoState == 6 || GetMemoState == 7) && st.ownItemCount(q_ridia_hairpin) >= 1)
			{
				st.takeItems(q_letter_of_ridia, -1);
				st.takeItems(q_ridia_hairpin, -1);
				st.setMemoState("people_of_lost_forest", String.valueOf(8), true);
				htmltext = "maid_of_ridia_q0024_06.htm";
			}
			else if((GetMemoState == 6 || GetMemoState == 7) && st.ownItemCount(q_ridia_hairpin) == 0)
			{
				st.setCond(7);
				st.setMemoState("people_of_lost_forest", String.valueOf(7), true);
				st.soundEffect(SOUND_MIDDLE);
				htmltext = "maid_of_ridia_q0024_07.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_10"))
		{
			if(GetMemoState == 8)
			{
				st.setMemoState("people_of_lost_forest", String.valueOf(9), true);
				htmltext = "maid_of_ridia_q0024_10.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_11"))
		{
			if(GetMemoState == 9)
			{
				st.setMemoState("people_of_lost_forest", String.valueOf(10), true);
				htmltext = "maid_of_ridia_q0024_14.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_12"))
		{
			if(GetMemoState == 10)
			{
				st.setCond(9);
				st.setMemoState("people_of_lost_forest", String.valueOf(11), true);
				st.soundEffect(SOUND_MIDDLE);
				htmltext = "maid_of_ridia_q0024_19.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_14"))
		{
			if(GetMemoState == 11 && st.ownItemCount(q_triols_totem1) >= 1)
			{
				st.setMemoState("people_of_lost_forest", String.valueOf(12), true);
				st.takeItems(q_triols_totem1, -1);
				htmltext = "shadow_hardin_q0024_03.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_16"))
		{
			if(GetMemoState == 12)
			{
				st.setCond(11);
				st.setMemoState("people_of_lost_forest", String.valueOf(13), true);
				htmltext = "shadow_hardin_q0024_08.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_18"))
		{
			if(GetMemoState == 13)
			{
				st.setMemoState("people_of_lost_forest", String.valueOf(14), true);
				htmltext = "shadow_hardin_q0024_17.htm";
			}
		}
		else if(event.equalsIgnoreCase("reply_19"))
			if(GetMemoState == 14)
			{
				st.giveItems(q_triols_totem2, 1);
				st.addExpAndSp(242105, 22529);
				st.removeMemo("people_of_lost_forest");
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
				htmltext = "shadow_hardin_q0024_21.htm";
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		QuestState qs = st.getPlayer().getQuestState(_023_LidiasHeart.class);
		int GetMemoState = st.getInt("people_of_lost_forest");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == day_dorian)
					if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 65)
						htmltext = "day_dorian_q0024_01.htm";
					else
					{
						htmltext = "day_dorian_q0024_02.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == day_dorian)
				{
					if(GetMemoState == 1)
						htmltext = "day_dorian_q0024_04.htm";
					else if(GetMemoState == 2)
						htmltext = "day_dorian_q0024_05.htm";
					else if(GetMemoState == 3)
						htmltext = "day_dorian_q0024_09.htm";
					else if(GetMemoState == 4 && st.ownItemCount(q_broken_silver_cross) >= 1)
						htmltext = "day_dorian_q0024_15.htm";
					else if(GetMemoState == 5)
						htmltext = "day_dorian_q0024_20.htm";
					else if(GetMemoState == 7 && st.ownItemCount(q_ridia_hairpin) == 0)
					{
						st.setCond(8);
						st.giveItems(q_ridia_hairpin, 1);
						st.soundEffect(SOUND_MIDDLE);
						htmltext = "day_dorian_q0024_21.htm";
					}
					else if(GetMemoState == 7 && st.ownItemCount(q_ridia_hairpin) >= 1 || GetMemoState == 6)
						htmltext = "day_dorian_q0024_22.htm";
				}
				else if(npcId == q_forest_stone2)
				{
					if(GetMemoState == 1 && st.ownItemCount(q_lost_flower) >= 1)
					{
						st.soundEffect(SOUND_D_WIND_LOOT_02);
						htmltext = "q_forest_stone2_q0024_01.htm";
					}
					else if(GetMemoState == 2)
						htmltext = "q_forest_stone2_q0024_03.htm";
				}
				else if(npcId == maid_of_ridia)
				{
					if(GetMemoState == 5)
						htmltext = "maid_of_ridia_q0024_01.htm";
					else if(GetMemoState == 6 && st.ownItemCount(q_letter_of_ridia) >= 1)
						htmltext = "maid_of_ridia_q0024_05.htm";
					else if(GetMemoState == 7)
						htmltext = "maid_of_ridia_q0024_07a.htm";
					else if(GetMemoState == 8)
						htmltext = "maid_of_ridia_q0024_08.htm";
					else if(GetMemoState == 9)
						htmltext = "maid_of_ridia_q0024_11.htm";
					else if(GetMemoState == 11)
						htmltext = "maid_of_ridia_q0024_20.htm";
				}
				else if(npcId == shadow_hardin)
					if(GetMemoState == 11 && st.ownItemCount(q_triols_totem1) >= 1)
						htmltext = "shadow_hardin_q0024_01.htm";
					else if(GetMemoState == 12)
						htmltext = "shadow_hardin_q0024_04.htm";
					else if(GetMemoState == 13)
						htmltext = "shadow_hardin_q0024_09.htm";
					else if(GetMemoState == 14)
						htmltext = "shadow_hardin_q0024_18.htm";
				break;
			case COMPLETED:
				if(npcId == shadow_hardin)
					htmltext = "shadow_hardin_q0024_22.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int GetMemoState = st.getInt("people_of_lost_forest");

		if(npcId == bone_snatcher || npcId == bone_snatcher_a || npcId == bone_shaper || npcId == bone_collector || npcId == skull_collector || npcId == bone_animator || npcId == skull_animator || npcId == bone_slayer)
			if(GetMemoState == 11 && Rnd.get(100) < 10)
			{
				st.setCond(10);
				st.giveItems(q_triols_totem1, 1);
				st.soundEffect(SOUND_ITEMGET);
			}
		return null;
	}
}
