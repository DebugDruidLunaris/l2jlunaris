package quests;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class _114_ResurrectionOfAnOldManager extends Quest implements ScriptFile
{
	// NPC
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	private static final int STONES = 32046;
	private static final int WENDY = 32047;
	private static final int BOX = 32050;

	// MOBS
	private static final int GUARDIAN = 27318;

	// QUEST ITEMS
	private static final int DETECTOR = 8090;
	private static final int DETECTOR2 = 8091;
	private static final int STARSTONE = 8287;
	private static final int LETTER = 8288;
	private static final int STARSTONE2 = 8289;

	private NpcInstance GUARDIAN_SPAWN = null;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _114_ResurrectionOfAnOldManager()
	{
		super(false);

		addStartNpc(YUMI);

		addTalkId(WENDY);
		addTalkId(BOX);
		addTalkId(STONES);
		addTalkId(NEWYEAR);
		addFirstTalkId(STONES);

		addKillId(GUARDIAN);

		addQuestItem(DETECTOR);
		addQuestItem(DETECTOR2);
		addQuestItem(STARSTONE);
		addQuestItem(LETTER);
		addQuestItem(STARSTONE2);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int choice;

		if(event.equalsIgnoreCase("head_blacksmith_newyear_q0114_02.htm"))
		{
			st.setCond(22);
			st.takeItems(LETTER, 1);
			st.giveItems(STARSTONE2, 1);
			st.soundEffect(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("collecter_yumi_q0114_04.htm"))
		{
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			st.setCond(1);
			st.setMemoState("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_08.htm"))
			st.setMemoState("talk", "1");
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_09.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_12.htm"))
		{
			choice = st.getInt("choice");
			if(choice == 1)
				htmltext = "collecter_yumi_q0114_12.htm";
			else if(choice == 2)
				htmltext = "collecter_yumi_q0114_13.htm";
			else if(choice == 3)
				htmltext = "collecter_yumi_q0114_14.htm";
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_15.htm"))
			st.setMemoState("talk", "1");
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_23.htm"))
			st.setMemoState("talk", "2");
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_26.htm"))
		{
			st.setCond(6);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_31.htm"))
		{
			st.setCond(17);
			st.soundEffect(SOUND_MIDDLE);
			st.giveItems(DETECTOR, 1);
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_34.htm"))
		{
			st.takeItems(DETECTOR2, 1);
			st.setMemoState("talk", "1");
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_38.htm"))
		{
			choice = st.getInt("choice");
			if(choice > 1)
				htmltext = "collecter_yumi_q0114_37.htm";
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_40.htm"))
		{
			st.setCond(21);
			st.giveItems(LETTER, 1);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("collecter_yumi_q0114_39.htm"))
		{
			st.setCond(20);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("pavel_atlanta_q0114_03.htm"))
		{
			st.setCond(19);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("pavel_atlanta_q0114_07.htm"))
		{
			st.soundEffect(SOUND_FINISH);
			st.addExpAndSp(1846611, 144270);
			st.exitQuest(false);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_01.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") == 2)
				htmltext = "chaos_secretary_wendy_q0114_05.htm";
			else if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6)
				htmltext = "chaos_secretary_wendy_q0114_06a.htm";
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_02.htm"))
		{
			if(st.getInt("talk") == 0)
				st.setMemoState("talk", "1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_03.htm"))
		{
			if(st.getInt("talk1") == 0)
				st.setMemoState("talk1", "1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_06.htm"))
		{
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
			st.setMemoState("choice", "1");
			st.removeMemo("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_07.htm"))
		{
			st.setCond(4);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
			st.setMemoState("choice", "2");
			st.removeMemo("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_09.htm"))
		{
			st.setCond(5);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
			st.setMemoState("choice", "3");
			st.removeMemo("talk1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_14ab.htm"))
		{
			st.setCond(7);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_14b.htm"))
		{
			st.setCond(10);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_12c.htm"))
		{
			if(st.getInt("talk") == 0)
				st.setMemoState("talk", "1");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_15b.htm"))
		{
			if(GUARDIAN_SPAWN == null || !st.getPlayer().knowsObject(GUARDIAN_SPAWN) || !GUARDIAN_SPAWN.isVisible())
			{
				GUARDIAN_SPAWN = st.addSpawn(GUARDIAN, 96977, -110625, -3280, 900000);
				Functions.npcSay(GUARDIAN_SPAWN, "You, " + st.getPlayer().getName() + ", you attacked Wendy. Prepare to die!");
				GUARDIAN_SPAWN.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, st.getPlayer(), 999);
			}
			else
				htmltext = "chaos_secretary_wendy_q0114_17b.htm";
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20b.htm"))
		{
			st.setCond(12);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_17c.htm"))
			st.setMemoState("talk", "2");
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20c.htm"))
		{
			st.setCond(13);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_23c.htm"))
		{
			st.setCond(15);
			st.soundEffect(SOUND_MIDDLE);
			st.takeItems(STARSTONE, 1);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_16a.htm"))
			st.setMemoState("talk", "2");
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_20a.htm"))
		{
			if(st.getCond() == 7)
			{
				st.setCond(8);
				st.setMemoState("talk", "0");
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(st.getCond() == 8)
			{
				st.setCond(9);
				st.soundEffect(SOUND_MIDDLE);
				htmltext = "chaos_secretary_wendy_q0114_21a.htm";
			}
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_21a.htm"))
		{
			st.setCond(9);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("chaos_secretary_wendy_q0114_29c.htm"))
		{
			st.giveItems(STARSTONE2, 1);
			st.takeItems(ADENA_ID, 3000);
			st.setCond(26);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("chaos_box2_q0114_01r.htm"))
		{
			st.soundEffect(SOUND_ARMOR_WOOD_3);
			st.setMemoState("talk", "1");
		}
		else if(event.equalsIgnoreCase("chaos_box2_q0114_03.htm"))
		{
			st.setCond(14);
			st.giveItems(STARSTONE, 1);
			st.soundEffect(SOUND_MIDDLE);
			st.setMemoState("talk", "0");
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if(st == null || st.isCompleted())
			return "";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == STONES && cond == 17)
		{
			st.soundEffect(SOUND_MIDDLE);
			st.takeItems(DETECTOR, 1);
			st.giveItems(DETECTOR2, 1);
			st.setCond(18);
			player.sendPacket(new ExShowScreenMessage(NpcString.THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE, 4500, ScreenMessageAlign.TOP_CENTER));
		}
		return "";
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		int talk = st.getInt("talk");
		int talk1 = st.getInt("talk1");
		if(npcId == YUMI)
		{
			if(id == CREATED)
			{
				QuestState Pavel = st.getPlayer().getQuestState(_121_PavelTheGiants.class);
				if(Pavel == null)
					return "collecter_yumi_q0114_01.htm";
				if(st.getPlayer().getLevel() >= 70 && Pavel.getState() == COMPLETED)
					htmltext = "collecter_yumi_q0114_02.htm";
				else
				{
					htmltext = "collecter_yumi_q0114_01.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
			{
				if(talk == 0)
					htmltext = "collecter_yumi_q0114_04.htm";
				else
					htmltext = "collecter_yumi_q0114_08.htm";
			}
			else if(cond == 2)
				htmltext = "collecter_yumi_q0114_10.htm";
			else if(cond == 3 || cond == 4 || cond == 5)
			{
				if(talk == 0)
					htmltext = "collecter_yumi_q0114_11.htm";
				else if(talk == 1)
					htmltext = "collecter_yumi_q0114_15.htm";
				else
					htmltext = "collecter_yumi_q0114_23.htm";
			}
			else if(cond == 6)
				htmltext = "collecter_yumi_q0114_27.htm";
			else if(cond == 9 || cond == 12 || cond == 16)
				htmltext = "collecter_yumi_q0114_28.htm";
			else if(cond == 17)
				htmltext = "collecter_yumi_q0114_32.htm";
			else if(cond == 19)
			{
				if(talk == 0)
					htmltext = "collecter_yumi_q0114_33.htm";
				else
					htmltext = "collecter_yumi_q0114_34.htm";
			}
			else if(cond == 20)
				htmltext = "collecter_yumi_q0114_39.htm";
			else if(cond == 21)
				htmltext = "collecter_yumi_q0114_40z.htm";
			else if(cond == 22 || cond == 26)
			{
				htmltext = "collecter_yumi_q0114_41.htm";
				st.setCond(27);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 27)
				htmltext = "collecter_yumi_q0114_42.htm";
		}
		else if(npcId == WENDY)
		{
			if(cond == 2)
			{
				if(talk + talk1 < 2)
					htmltext = "chaos_secretary_wendy_q0114_01.htm";
				else if(talk + talk1 == 2)
					htmltext = "chaos_secretary_wendy_q0114_05.htm";
			}
			else if(cond == 3)
				htmltext = "chaos_secretary_wendy_q0114_06b.htm";
			else if(cond == 4 || cond == 5)
				htmltext = "chaos_secretary_wendy_q0114_08.htm";
			else if(cond == 6)
			{
				int choice = st.getInt("choice");
				if(choice == 1)
				{
					if(talk == 0)
						htmltext = "chaos_secretary_wendy_q0114_11a.htm";
					else if(talk == 1)
						htmltext = "chaos_secretary_wendy_q0114_17c.htm";
					else
						htmltext = "chaos_secretary_wendy_q0114_16a.htm";
				}
				else if(choice == 2)
					htmltext = "chaos_secretary_wendy_q0114_11b.htm";
				else if(choice == 3)
					if(talk == 0)
						htmltext = "chaos_secretary_wendy_q0114_11c.htm";
					else if(talk == 1)
						htmltext = "chaos_secretary_wendy_q0114_12c.htm";
					else
						htmltext = "chaos_secretary_wendy_q0114_17c.htm";
			}
			else if(cond == 7)
			{
				if(talk == 0)
					htmltext = "chaos_secretary_wendy_q0114_11c.htm";
				else if(talk == 1)
					htmltext = "chaos_secretary_wendy_q0114_12c.htm";
				else
					htmltext = "chaos_secretary_wendy_q0114_17c.htm";
			}
			else if(cond == 8)
				htmltext = "chaos_secretary_wendy_q0114_16a.htm";
			else if(cond == 9)
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			else if(cond == 10)
				htmltext = "chaos_secretary_wendy_q0114_18b.htm";
			else if(cond == 11)
				htmltext = "chaos_secretary_wendy_q0114_19b.htm";
			else if(cond == 12)
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			else if(cond == 13)
				htmltext = "chaos_secretary_wendy_q0114_20c.htm";
			else if(cond == 14)
				htmltext = "chaos_secretary_wendy_q0114_22c.htm";
			else if(cond == 15)
			{
				htmltext = "chaos_secretary_wendy_q0114_24c.htm";
				st.setCond(16);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 16)
				htmltext = "chaos_secretary_wendy_q0114_25c.htm";
			else if(cond == 20)
				htmltext = "chaos_secretary_wendy_q0114_26c.htm";
			else if(cond == 26)
				htmltext = "chaos_secretary_wendy_q0114_32c.htm";
		}
		else if(npcId == BOX)
		{
			if(cond == 13)
			{
				if(talk == 0)
					htmltext = "chaos_box2_q0114_01.htm";
				else
					htmltext = "chaos_box2_q0114_02.htm";
			}
			else if(cond == 14)
				htmltext = "chaos_box2_q0114_04.htm";
		}
		else if(npcId == STONES)
		{
			if(cond == 18)
				htmltext = "pavel_atlanta_q0114_02.htm";
			else if(cond == 19)
				htmltext = "pavel_atlanta_q0114_03.htm";
			else if(cond == 27)
				htmltext = "pavel_atlanta_q0114_04.htm";
		}
		else if(npcId == NEWYEAR)
			if(cond == 21)
				htmltext = "head_blacksmith_newyear_q0114_01.htm";
			else if(cond == 22)
				htmltext = "head_blacksmith_newyear_q0114_03.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;

		int npcId = npc.getNpcId();
		if(st.getCond() == 10)
			if(npcId == GUARDIAN)
			{
				Functions.npcSay(npc, "This enemy is far too powerful for me to fight. I must withdraw");
				st.setCond(11);
				st.soundEffect(SOUND_MIDDLE);
			}
		return null;
	}
}