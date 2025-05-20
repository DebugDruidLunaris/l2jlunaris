package quests;

import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

/**
 * Based on official H5
 * @author Magister
 */
public class _409_PathToOracle extends Quest implements ScriptFile
{
	//npc
	private final static int father_manuell = 30293;
	private final static int allana = 30424;
	private final static int perrin = 30428;

	//mobs
	private final static int q409_lizardman_warrior = 27032;
	private final static int q409_lizardman_scout = 27033;
	private final static int q409_lizardman = 27034;
	private final static int tamato = 27035;

	//questitem
	private final static int crystal_medallion = 1231;
	private final static int money_of_swindler = 1232;
	private final static int dairy_of_allana = 1233;
	private final static int lizard_captain_order = 1234;
	private final static int leaf_of_oracle = 1235;
	private final static int half_of_dairy = 1236;
	private final static int tamatos_necklace = 1275;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _409_PathToOracle()
	{
		super(false);
		addStartNpc(father_manuell);
		addTalkId(new int[] { father_manuell, allana, perrin });
		addKillId(new int[] { q409_lizardman_warrior, q409_lizardman_scout, q409_lizardman, tamato });
		addQuestItem(new int[] { money_of_swindler, dairy_of_allana, lizard_captain_order, crystal_medallion, half_of_dairy, tamatos_necklace });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int GetMemoState = st.getInt("path_to_oracle");
		int elven_mage = 0x19;
		int oracle = 0x1d;
		int talker_occupation = st.getPlayer().getClassId().getId();
		int npcId = npc.getNpcId();

		if(npcId == father_manuell)
		{
			if(event.equalsIgnoreCase("quest_accept"))
				if(st.getPlayer().getLevel() >= 18 && talker_occupation == elven_mage && st.ownItemCount(leaf_of_oracle) == 0)
				{
					st.setCond(1);
					st.setMemoState("path_to_oracle", String.valueOf(1), true);
					st.soundEffect(SOUND_ACCEPT);
					st.giveItems(crystal_medallion, 1);
					htmltext = "father_manuell_q0409_05.htm";
					st.setState(STARTED);
				}
				else if(talker_occupation != elven_mage)
				{
					if(talker_occupation == oracle)
					{
						htmltext = "father_manuell_q0409_02a.htm";
						st.exitQuest(true);
					}
					else
					{
						htmltext = "father_manuell_q0409_02.htm";
						st.exitQuest(true);
					}
				}
				else if(st.getPlayer().getLevel() < 18 && talker_occupation == elven_mage)
				{
					htmltext = "father_manuell_q0409_03.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() >= 18 && talker_occupation == elven_mage && st.ownItemCount(leaf_of_oracle) == 1)
					htmltext = "father_manuell_q0409_04.htm";
		}
		else if(npcId == allana)
		{
			if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 1)
					htmltext = "allana_q0409_07.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
				htmltext = "allana_q0409_08.htm";
			else if(event.equalsIgnoreCase("reply_3"))
				htmltext = "allana_q0409_09.htm";
			else if(event.equalsIgnoreCase("reply_4"))
			{
				// проверяем есть ли в мире уже квест монстры.
				int spawn = 0;
				NpcInstance isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman_warrior);
				if(isQuestMonster != null)
					spawn = 1;
				isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman_scout);
				if(isQuestMonster != null)
					spawn = 1;
				isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman);
				if(isQuestMonster != null)
					spawn = 1;
				if(spawn == 1) // если хоть один моб есть в мире, ставим таймер на удаление их(на всякий) + говорим игроку подождать.
				{
					if(!st.isRunningQuestTimer("wait"))
						st.startQuestTimer("wait", 300000);
					htmltext = "<html><head><body>Please wait 5 minutes...</body></html>";
				}
				else
				{
					st.cancelQuestTimer("wait");
					st.startQuestTimer("q409_lizardman_warrior_QuestMonster", 200000);
					st.startQuestTimer("q409_lizardman_scout_QuestMonster", 200000);
					st.startQuestTimer("q409_lizardman_QuestMonster", 200000);
					st.addSpawn(q409_lizardman_warrior);
					st.addSpawn(q409_lizardman_scout);
					st.addSpawn(q409_lizardman);
					st.setMemoState("path_to_oracle", String.valueOf(2), true);
				}
			}
			else if(event.equalsIgnoreCase("wait") || event.equalsIgnoreCase("q409_lizardman_warrior_QuestMonster") || event.equalsIgnoreCase("q409_lizardman_scout_QuestMonster") || event.equalsIgnoreCase("q409_lizardman_QuestMonster"))
			{
				NpcInstance isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman_warrior);
				if(isQuestMonster != null)
					isQuestMonster.deleteMe();
				isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman_scout);
				if(isQuestMonster != null)
					isQuestMonster.deleteMe();
				isQuestMonster = GameObjectsStorage.getByNpcId(q409_lizardman);
				if(isQuestMonster != null)
					isQuestMonster.deleteMe();
				st.cancelQuestTimer("wait");
				return null;
			}
		}
		else if(npcId == perrin)
			if(event.equalsIgnoreCase("reply_1"))
			{
				if(GetMemoState == 2)
					htmltext = "perrin_q0409_02.htm";
			}
			else if(event.equalsIgnoreCase("reply_2"))
			{
				if(GetMemoState == 2)
					htmltext = "perrin_q0409_03.htm";
			}
			else if(event.equalsIgnoreCase("reply_3"))
				if(GetMemoState == 2)
				{
					int spawn = 0;
					NpcInstance isQuestMonster = GameObjectsStorage.getByNpcId(tamato);
					if(isQuestMonster != null)
						spawn = 1;
					if(spawn == 1) // если хоть один моб есть в мире, ставим таймер на удаление их(на всякий) + говорим игроку подождать.
					{
						if(!st.isRunningQuestTimer("wait1"))
							st.startQuestTimer("wait1", 300000);
						htmltext = "<html><head><body>Please wait 5 minutes...</body></html>";
					}
					else
					{
						st.cancelQuestTimer("wait1");
						st.startQuestTimer("tamato", 200000);
						st.addSpawn(tamato);
						st.setMemoState("path_to_oracle", String.valueOf(3), true);
					}
				}
				else if(event.equalsIgnoreCase("wait1") || event.equalsIgnoreCase("tamato"))
				{
					NpcInstance isQuestMonster = GameObjectsStorage.getByNpcId(tamato);
					if(isQuestMonster != null)
						isQuestMonster.deleteMe();
					st.cancelQuestTimer("wait1");
					return null;
				}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int GetMemoState = st.getInt("path_to_oracle");
		int npcId = npc.getNpcId();
		int id = st.getState();

		switch(id)
		{
			case CREATED:
				if(npcId == father_manuell)
					if(st.ownItemCount(leaf_of_oracle) == 0)
						htmltext = "father_manuell_q0409_01.htm";
					else
						htmltext = "father_manuell_q0409_04.htm";
				break;
			case STARTED:
				if(npcId == father_manuell)
				{
					if(st.ownItemCount(crystal_medallion) > 0)
						if(st.ownItemCount(money_of_swindler) == 0 && st.ownItemCount(dairy_of_allana) == 0 && st.ownItemCount(lizard_captain_order) == 0 && st.ownItemCount(half_of_dairy) == 0)
						{
							if(GetMemoState == 2)
							{
								st.setCond(8);
								st.setMemoState("path_to_oracle", String.valueOf(1), true);
								htmltext = "father_manuell_q0409_09.htm";
							}
							else
							{
								st.setMemoState("path_to_oracle", String.valueOf(1), true);
								htmltext = "father_manuell_q0409_06.htm";
							}
						}
						else if(st.ownItemCount(money_of_swindler) == 1 && st.ownItemCount(dairy_of_allana) == 1 && st.ownItemCount(lizard_captain_order) == 1 && st.ownItemCount(half_of_dairy) == 0)
						{
							htmltext = "father_manuell_q0409_08.htm";
							st.takeItems(money_of_swindler, 1);
							st.takeItems(dairy_of_allana, 1);
							st.takeItems(lizard_captain_order, 1);
							st.takeItems(crystal_medallion, 1);
							st.giveItems(leaf_of_oracle, 1);

							if(st.getPlayer().getLevel() >= 20)
								st.addExpAndSp(320534, 20392);
							else if(st.getPlayer().getLevel() == 19)
								st.addExpAndSp(456128, 27090);
							else
								st.addExpAndSp(591724, 33788);
							st.giveItems(ADENA_ID, 163800);
							st.removeMemo("path_to_oracle");
							st.soundEffect(SOUND_FINISH);
							st.exitQuest(false);
						}
						else
							htmltext = "father_manuell_q0409_07.htm";
				}
				else if(npcId == allana)
				{
					if(st.ownItemCount(money_of_swindler) == 0 && st.ownItemCount(dairy_of_allana) == 0 && st.ownItemCount(lizard_captain_order) == 0 && st.ownItemCount(half_of_dairy) == 0)
					{
						if(GetMemoState == 2)
							htmltext = "allana_q0409_05.htm";
						else if(GetMemoState == 1)
						{
							htmltext = "allana_q0409_01.htm";
							st.setCond(2);
							st.soundEffect(SOUND_MIDDLE);
						}
					}
					else if(st.ownItemCount(money_of_swindler) == 0 && st.ownItemCount(dairy_of_allana) == 0 && st.ownItemCount(lizard_captain_order) == 1 && st.ownItemCount(half_of_dairy) == 0)
					{
						st.setCond(4);
						st.setMemoState("path_to_oracle", String.valueOf(2), true);
						htmltext = "allana_q0409_02.htm";
						st.giveItems(half_of_dairy, 1);
						st.soundEffect(SOUND_MIDDLE);
					}
					else if(st.ownItemCount(money_of_swindler) == 0 && st.ownItemCount(dairy_of_allana) == 0 && st.ownItemCount(lizard_captain_order) == 1 && st.ownItemCount(half_of_dairy) == 1)
					{
						if(GetMemoState == 3 && st.ownItemCount(tamatos_necklace) == 0)
						{
							st.setCond(4);
							st.setMemoState("path_to_oracle", String.valueOf(2), true);
							htmltext = "allana_q0409_06.htm";
							st.soundEffect(SOUND_MIDDLE);
						}
						else
							htmltext = "allana_q0409_03.htm";
					}
					else if(st.ownItemCount(money_of_swindler) == 1 && st.ownItemCount(dairy_of_allana) == 0 && st.ownItemCount(lizard_captain_order) == 1 && st.ownItemCount(half_of_dairy) == 1)
					{
						htmltext = "allana_q0409_04.htm";
						st.takeItems(half_of_dairy, 1);
						st.giveItems(dairy_of_allana, 1);
						st.setCond(9);
						st.soundEffect(SOUND_MIDDLE);
					}
					else if(st.ownItemCount(money_of_swindler) == 1 && st.ownItemCount(lizard_captain_order) == 1 && st.ownItemCount(half_of_dairy) == 0 && st.ownItemCount(dairy_of_allana) > 0)
					{
						htmltext = "allana_q0409_05.htm";
						st.setCond(7);
						st.soundEffect(SOUND_MIDDLE);
					}
				}
				else if(npcId == perrin)
					if(st.ownItemCount(tamatos_necklace) == 1)
					{
						st.giveItems(money_of_swindler, 1);
						st.takeItems(tamatos_necklace, 1);
						htmltext = "perrin_q0409_04.htm";
						st.setCond(6);
						st.soundEffect(SOUND_MIDDLE);
					}
					else if(st.ownItemCount(money_of_swindler) > 0)
						htmltext = "perrin_q0409_05.htm";
					else if(GetMemoState == 3)
						htmltext = "perrin_q0409_06.htm";
					else
						htmltext = "perrin_q0409_01.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onAttack(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		if((npcId == q409_lizardman_warrior || npcId == q409_lizardman_scout || npcId == q409_lizardman) && npc != null)
			Functions.npcSay(npc, "The sacred flame is ours!");
		else if(npcId == tamato && npc != null)
			Functions.npcSay(npc, "As you wish, master!");
		return null;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		if(npcId == q409_lizardman_warrior || npcId == q409_lizardman_scout || npcId == q409_lizardman)
		{
			if(st.ownItemCount(lizard_captain_order) == 0 && npc != null)
			{
				Functions.npcSay(npc, "Arrghh...we shall never.. surrender...");
				st.giveItems(lizard_captain_order, 1);
				st.setCond(3);
				st.soundEffect(SOUND_MIDDLE);
			}
		}
		else if(npcId == tamato)
			if(st.ownItemCount(tamatos_necklace) == 0 && npc != null)
			{
				st.giveItems(tamatos_necklace, 1);
				st.setCond(5);
				st.soundEffect(SOUND_MIDDLE);
			}
		return null;
	}
}