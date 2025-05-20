package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;

public class _064_CertifiedBerserker extends Quest implements ScriptFile
{
	// NPC
	private static final int ORKURUS = 32207;
	private static final int TENAIN = 32215;
	private static final int GORT = 32252;
	private static final int HARKILGAMED = 32236;
	private static final int ENTIEN = 32200;

	// Mobs
	private static final int BREKA_ORC = 20267;
	private static final int BREKA_ORC_ARCHER = 20268;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int BREKA_ORC_WARRIOR = 20271;
	private static final int ROAD_SCAVENGER = 20551;
	private static final int DEAD_SEEKER = 20202;
	private static final int STAKATO = 20234;
	private static final int DIVINE = 27323;

	// Quest Item
	private static final int Dimenional_Diamonds = 7562;
	private static final int BREKA_ORC_HEAD = 9754;
	private static final int MESSAGE_PLATE = 9755;
	private static final int REPORT1 = 9756;
	private static final int REPORT2 = 9757;
	private static final int H_LETTER = 9758;
	private static final int T_REC = 9759;
	private static final int OrkurusRecommendation = 9760;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public NpcInstance HARKILGAMED_SPAWN;

	private void DESPAWN_HARKILGAMED()
	{
		if(HARKILGAMED_SPAWN != null)
			HARKILGAMED_SPAWN.deleteMe();
		HARKILGAMED_SPAWN = null;
	}

	private void SPAWN_HARKILGAMED(QuestState st)
	{
		HARKILGAMED_SPAWN = Functions.spawn(Location.findPointToStay(st.getPlayer(), 50, 100), HARKILGAMED);
	}

	public _064_CertifiedBerserker()
	{
		super(false);

		addStartNpc(ORKURUS);

		addTalkId(ORKURUS);
		addTalkId(TENAIN);
		addTalkId(GORT);
		addTalkId(ENTIEN);
		addTalkId(HARKILGAMED);

		addKillId(BREKA_ORC);
		addKillId(BREKA_ORC_ARCHER);
		addKillId(BREKA_ORC_SHAMAN);
		addKillId(BREKA_ORC_OVERLORD);
		addKillId(BREKA_ORC_WARRIOR);
		addKillId(ROAD_SCAVENGER);
		addKillId(DEAD_SEEKER);
		addKillId(STAKATO);
		addKillId(DIVINE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32207-01a.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			if(!st.getPlayer().getVarB("dd1"))
			{
				st.giveItems(Dimenional_Diamonds, 48);
				st.getPlayer().setVar("dd1", "1", -1);
			}
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32215-01a.htm"))
			st.setCond(2);
		else if(event.equalsIgnoreCase("32252-01a.htm"))
			st.setCond(5);
		else if(event.equalsIgnoreCase("32215-03d.htm"))
		{
			st.takeItems(MESSAGE_PLATE, -1);
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("32236-01a.htm"))
		{
			st.setCond(13);
			st.giveItems(H_LETTER, 1);
			st.cancelQuestTimer("HARKILGAMED_Fail");
			DESPAWN_HARKILGAMED();
		}
		else if(event.equalsIgnoreCase("32215-05a.htm"))
		{
			st.setCond(14);
			st.takeItems(H_LETTER, -1);
			st.giveItems(T_REC, 1);
		}
		else if(event.equalsIgnoreCase("32207-03a.htm"))
		{
			if(!st.getPlayer().getVarB("prof2.1"))
			{
				st.addExpAndSp(174503, 11973);
				st.giveItems(ADENA_ID, 31552);
				st.getPlayer().setVar("prof2.1", "1", -1);
			}
			st.giveItems(OrkurusRecommendation, 1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		if(event.equalsIgnoreCase("HARKILGAMED_Fail"))
		{
			DESPAWN_HARKILGAMED();
			return null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == ORKURUS)
		{
			if(st.ownItemCount(OrkurusRecommendation) != 0)
			{
				htmltext = "32207-00.htm";
				st.exitQuest(true);
			}
			else if(cond == 0)
			{
				if(st.getPlayer().getClassId().getId() == 0x7D)
				{
					if(st.getPlayer().getLevel() >= 39)
						htmltext = "32207-01.htm";
					else
					{
						htmltext = "32207-02.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32207-02a.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 14)
			{
				st.takeItems(T_REC, -1);
				htmltext = "32207-03.htm";
			}

		}
		else if(npcId == TENAIN)
		{
			if(cond == 1)
				htmltext = "32215-01.htm";
			else if(cond == 3)
			{
				htmltext = "32215-02.htm";
				st.takeItems(BREKA_ORC_HEAD, -1);
				st.setCond(4);
			}
			else if(cond > 1 && st.ownItemCount(BREKA_ORC_HEAD) == 20)
			{
				htmltext = "32215-02.htm";
				st.takeItems(BREKA_ORC_HEAD, -1);
				st.setCond(4);
			}
			else if(cond == 7)
				htmltext = "32215-03.htm";
			else if(cond == 11)
			{
				st.setCond(12);
				htmltext = "32215-04.htm";
			}
			else if(cond == 13)
			{
				st.setCond(14);
				htmltext = "32215-05.htm";
			}

		}
		else if(npcId == GORT)
		{
			if(cond == 4)
				htmltext = "32252-01.htm";
			else if(cond == 6)
			{
				htmltext = "32252-02.htm";
				st.setCond(7);
			}
			else if(cond > 4 && st.ownItemCount(MESSAGE_PLATE) == 1)
			{
				htmltext = "32252-02.htm";
				st.setCond(7);
			}
		}
		else if(npcId == ENTIEN)
		{
			if(cond == 8)
			{
				st.setCond(9);
				htmltext = "32200-01.htm";
			}
			else if(cond == 10)
			{
				st.setCond(11);
				st.takeItems(REPORT1, -1);
				st.takeItems(REPORT2, -1);
				htmltext = "32200-02.htm";
			}
		}
		else if(npcId == HARKILGAMED)
			if(cond == 12)
				htmltext = "32236-01.htm";
		return htmltext;

	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2)
			if(npcId == BREKA_ORC || npcId == BREKA_ORC_ARCHER || npcId == BREKA_ORC_SHAMAN || npcId == BREKA_ORC_OVERLORD || npcId == BREKA_ORC_WARRIOR)
				if(st.ownItemCount(BREKA_ORC_HEAD) <= 19)
				{
					st.giveItems(BREKA_ORC_HEAD, 1);
					if(st.ownItemCount(BREKA_ORC_HEAD) == 20)
					{
						st.soundEffect(SOUND_MIDDLE);
						st.setCond(3);
					}
					else
						st.soundEffect(SOUND_ITEMGET);
				}
		if(cond == 5 && npcId == ROAD_SCAVENGER && Rnd.chance(20) && st.ownItemCount(MESSAGE_PLATE) == 0)
		{
			st.giveItems(MESSAGE_PLATE, 1);
			st.setCond(6);
			st.soundEffect(SOUND_MIDDLE);
		}
		if(cond == 9 && Rnd.chance(30))
		{
			if(npcId == DEAD_SEEKER && st.ownItemCount(REPORT1) == 0)
				st.giveItems(REPORT1, 1);
			else if(npcId == STAKATO && st.ownItemCount(REPORT2) == 0)
				st.giveItems(REPORT2, 1);
			if(st.ownItemCount(REPORT1) == 1 && st.ownItemCount(REPORT2) == 1)
			{
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(10);
			}
			else
				st.soundEffect(SOUND_ITEMGET);
		}
		if(cond == 12 && npcId == DIVINE && Rnd.chance(35))
		{
			DESPAWN_HARKILGAMED();
			SPAWN_HARKILGAMED(st);
			st.soundEffect(SOUND_MIDDLE);
			if(!st.isRunningQuestTimer("HARKILGAMED_Fail"))
				st.startQuestTimer("HARKILGAMED_Fail", 120000);
		}
		return null;
	}
}