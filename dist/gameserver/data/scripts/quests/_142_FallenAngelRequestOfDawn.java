package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _142_FallenAngelRequestOfDawn extends Quest implements ScriptFile
{
	// npc
	private final static int warehouse_chief_natools = 30894;
	private final static int bishop_raimund = 30289;
	private final static int sage_kasian = 30612;
	private final static int stained_rock = 32368;

	// questitem
	private final static int q_angel_encode = 10351;
	private final static int q_prophetic_book = 10352;
	private final static int q_blood_of_angel = 10353;

	// Monsters
	private final static int ant = 20079;
	private final static int ant_captain = 20080;
	private final static int ant_overseer = 20081;
	private final static int ant_recruit = 20082;
	private final static int ant_patrol = 20084;
	private final static int ant_guard = 20086;
	private final static int ant_soldier = 20087;
	private final static int ant_warrior_captain = 20088;
	private final static int noble_ant = 20089;
	private final static int noble_ant_leader = 20090;
	private final static int q_fallen_angel_mon = 27338;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _142_FallenAngelRequestOfDawn()
	{
		super(false);
		addStartNpc(warehouse_chief_natools);
		addTalkId(warehouse_chief_natools, bishop_raimund, sage_kasian, stained_rock);
		addQuestItem(q_angel_encode, q_prophetic_book, q_blood_of_angel);
		addKillId(ant, ant_captain, ant_overseer, ant_recruit, ant_patrol, ant_guard, ant_soldier, ant_warrior_captain, noble_ant, noble_ant_leader, q_fallen_angel_mon);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("fallen_angel_4a");
		int spawned_q_fallen_angel_mon = st.getInt("spawned_q_fallen_angel_mon");
		String q_fallen_angel_mon_player_name = st.get("q_fallen_angel_mon_player_name");
		int npcId = npc.getNpcId();

		if(npcId == warehouse_chief_natools)
		{
			if(event.equalsIgnoreCase("quest_accept"))
			{
				st.setCond(1);
				st.setMemoState("fallen_angel_4a", String.valueOf(1), true);
				st.setState(STARTED);
				st.soundEffect(SOUND_ACCEPT);
				htmltext = "warehouse_chief_natools_q0142_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_1"))
				htmltext = "warehouse_chief_natools_q0142_02.htm";
			else if(event.equalsIgnoreCase("reply_2"))
				htmltext = "warehouse_chief_natools_q0142_03.htm";
			else if(event.equalsIgnoreCase("reply_3"))
				htmltext = "warehouse_chief_natools_q0142_04.htm";
			else if(event.equalsIgnoreCase("reply_4"))
			{
				if(GetMemoState == 1)
					htmltext = "warehouse_chief_natools_q0142_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
				if(GetMemoState == 1)
				{
					st.setCond(2);
					st.setMemoState("fallen_angel_4a", String.valueOf(2), true);
					st.giveItems(q_angel_encode, 1);
					st.soundEffect(SOUND_MIDDLE);
					htmltext = "warehouse_chief_natools_q0142_10.htm";
				}
		}
		else if(npcId == bishop_raimund)
		{
			if(event.equalsIgnoreCase("reply_3"))
			{
				if(GetMemoState == 3)
					htmltext = "bishop_raimund_q0142_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_4"))
			{
				if(GetMemoState == 3)
					htmltext = "bishop_raimund_q0142_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_5"))
				if(GetMemoState == 3)
				{
					st.setCond(3);
					st.setMemoState("fallen_angel_4a", String.valueOf(4), true);
					st.soundEffect(SOUND_MIDDLE);
					htmltext = "bishop_raimund_q0142_05.htm";
				}
		}
		else if(npcId == sage_kasian)
		{
			if(event.equalsIgnoreCase("reply_5"))
			{
				if(GetMemoState == 5)
					htmltext = "sage_kasian_q0142_04.htm";
			}
			else if(event.equalsIgnoreCase("reply_6"))
			{
				if(GetMemoState == 5)
					htmltext = "sage_kasian_q0142_05.htm";
			}
			else if(event.equalsIgnoreCase("reply_7"))
			{
				if(GetMemoState == 5)
				{
					st.setMemoState("fallen_angel_4a", String.valueOf(6), true);
					htmltext = "sage_kasian_q0142_06.htm";
				}
			}
			else if(event.equalsIgnoreCase("reply_8"))
			{
				if(GetMemoState == 6)
					htmltext = "sage_kasian_q0142_08.htm";
			}
			else if(event.equalsIgnoreCase("reply_9"))
			{
				if(GetMemoState == 6)
					htmltext = "sage_kasian_q0142_09.htm";
			}
			else if(event.equalsIgnoreCase("reply_10"))
				if(GetMemoState == 6)
				{
					st.setCond(4);
					st.setMemoState("fallen_angel_4a", String.valueOf(7), true);
					st.soundEffect(SOUND_MIDDLE);
					htmltext = "sage_kasian_q0142_10.htm";
				}
		}
		else if(npcId == stained_rock)
			if(event.equalsIgnoreCase("reply_1"))
			{
				if(spawned_q_fallen_angel_mon == 0)
				{
					st.setMemoState("spawned_q_fallen_angel_mon", String.valueOf(1), true);
					st.setMemoState("q_fallen_angel_mon_player_name", st.getPlayer().getName(), true);
					NpcInstance fallen_angel = st.addSpawn(q_fallen_angel_mon, st.getPlayer().getX() + 100, st.getPlayer().getY() + 100, st.getPlayer().getZ());
					st.startQuestTimer("14201", 120000, fallen_angel);
				}
				else if(q_fallen_angel_mon_player_name == st.getPlayer().getName())
					htmltext = "stained_rock_q0142_04.htm";
				else
					htmltext = "stained_rock_q0142_03.htm";
			}
			else if(event.equalsIgnoreCase("14201"))
			{
				st.removeMemo("spawned_q_fallen_angel_mon");
				if(npc != null)
					npc.deleteMe();
				return null;
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		QuestState qs = st.getPlayer().getQuestState(_141_ShadowFoxPart3.class);
		int GetMemoState = st.getInt("fallen_angel_4a");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == warehouse_chief_natools)
					if(st.getPlayer().getLevel() >= 38 && qs != null && qs.isCompleted())
						htmltext = "warehouse_chief_natools_q0142_01.htm";
					else
					{
						htmltext = "warehouse_chief_natools_q0142_05.htm";
						st.exitQuest(true);
					}
				break;
			case STARTED:
				if(npcId == warehouse_chief_natools)
				{
					if(GetMemoState == 1)
						htmltext = "warehouse_chief_natools_q0142_08.htm";
					else if(GetMemoState >= 2)
						htmltext = "warehouse_chief_natools_q0142_11.htm";
				}
				else if(npcId == bishop_raimund)
				{
					if(GetMemoState < 2)
						htmltext = "bishop_raimund_q0142_01.htm";
					else if(GetMemoState == 2)
					{
						st.takeItems(q_angel_encode, -1);
						st.setMemoState("fallen_angel_4a", String.valueOf(3), true);
						htmltext = "bishop_raimund_q0142_02.htm";
					}
					else if(GetMemoState == 3)
						htmltext = "bishop_raimund_q0142_02a.htm";
					else if(GetMemoState >= 4 && GetMemoState < 9)
						htmltext = "bishop_raimund_q0142_06.htm";
					else if(GetMemoState >= 9)
					{
						st.takeItems(q_blood_of_angel, -1);
						st.giveItems(ADENA_ID, 92676);
						if(st.getPlayer().getLevel() < 44)
							st.addExpAndSp(223036, 13901);
						st.removeMemo("fallen_angel_4a");
						st.removeMemo("spawned_q_fallen_angel_mon");
						st.removeMemo("q_fallen_angel_mon_player_name");
						st.soundEffect(SOUND_FINISH);
						st.exitQuest(false);
						htmltext = "bishop_raimund_q0142_07.htm";
					}
				}
				else if(npcId == sage_kasian)
				{
					if(GetMemoState < 4)
						htmltext = "sage_kasian_q0142_01.htm";
					else if(GetMemoState == 4)
					{
						st.setMemoState("fallen_angel_4a", String.valueOf(5), true);
						htmltext = "sage_kasian_q0142_02.htm";
					}
					else if(GetMemoState == 5)
						htmltext = "sage_kasian_q0142_03.htm";
					else if(GetMemoState == 6)
						htmltext = "sage_kasian_q0142_07.htm";
					else if(GetMemoState >= 7)
						htmltext = "sage_kasian_q0142_11.htm";
				}
				else if(npcId == stained_rock)
					if(GetMemoState < 8)
						htmltext = "stained_rock_q0142_01.htm";
					else if(GetMemoState == 8)
						htmltext = "stained_rock_q0142_02.htm";
					else if(GetMemoState > 8)
						htmltext = "stained_rock_q0142_06.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int GetMemoState = st.getInt("fallen_angel_4a");
		int spawned_q_fallen_angel_mon = st.getInt("spawned_q_fallen_angel_mon");
		int npcId = npc.getNpcId();

		if(GetMemoState == 7)
			if(npcId == ant)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 338)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_captain)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 363)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_overseer)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 611)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_recruit || npcId == ant_guard)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 371)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_patrol)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 421)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_soldier)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 900)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == ant_warrior_captain)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 1000)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == noble_ant)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 413)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == noble_ant_leader)
			{
				int i0 = Rnd.get(1000);
				if(i0 < 917)
					if(st.ownItemCount(q_prophetic_book) >= 29)
					{
						st.setCond(5);
						st.setMemoState("fallen_angel_4a", String.valueOf(8), true);
						st.takeItems(q_prophetic_book, -1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else
					{
						st.giveItems(q_prophetic_book, 1);
						st.soundEffect(SOUND_ITEMGET);
					}
			}
			else if(npcId == q_fallen_angel_mon)
				if(spawned_q_fallen_angel_mon == 1)
					if(GetMemoState == 8)
					{
						st.setCond(6);
						st.setMemoState("fallen_angel_4a", String.valueOf(9), true);
						st.giveItems(q_blood_of_angel, 1);
						st.soundEffect(SOUND_MIDDLE);
					}
		return null;
	}
}