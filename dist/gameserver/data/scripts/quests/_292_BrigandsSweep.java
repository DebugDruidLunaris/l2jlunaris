package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Квест Brigands Sweep
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _292_BrigandsSweep extends Quest implements ScriptFile
{
	// NPCs
	private static int Spiron = 30532;
	private static int Balanki = 30533;
	// Mobs
	private static int GoblinBrigand = 20322;
	private static int GoblinBrigandLeader = 20323;
	private static int GoblinBrigandLieutenant = 20324;
	private static int GoblinSnooper = 20327;
	private static int GoblinLord = 20528;
	// Quest Items
	private static int GoblinNecklace = 1483;
	private static int GoblinPendant = 1484;
	private static int GoblinLordPendant = 1485;
	private static int SuspiciousMemo = 1486;
	private static int SuspiciousContract = 1487;
	// Chances
	private static int Chance = 10;
	//Drop Cond
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	private static final int[][] DROPLIST_COND = {
			{ 1, 0, GoblinBrigand, 0, GoblinNecklace, 0, 40, 1 },
			{ 1, 0, GoblinBrigandLeader, 0, GoblinNecklace, 0, 40, 1 },
			{ 1, 0, GoblinSnooper, 0, GoblinNecklace, 0, 40, 1 },
			{ 1, 0, GoblinBrigandLieutenant, 0, GoblinPendant, 0, 40, 1 },
			{ 1, 0, GoblinLord, 0, GoblinLordPendant, 0, 40, 1 } };

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _292_BrigandsSweep()
	{
		super(false);
		addStartNpc(Spiron);
		addTalkId(Balanki);
		//Mob Drop
		for(int i = 0; i < DROPLIST_COND.length; i++)
			addKillId(DROPLIST_COND[i][2]);
		addQuestItem(SuspiciousMemo);
		addQuestItem(SuspiciousContract);
		addQuestItem(GoblinNecklace);
		addQuestItem(GoblinPendant);
		addQuestItem(GoblinLordPendant);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("elder_spiron_q0292_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("elder_spiron_q0292_06.htm"))
		{
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == Spiron)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.dwarf)
				{
					htmltext = "elder_spiron_q0292_00.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() < 5)
				{
					htmltext = "elder_spiron_q0292_01.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "elder_spiron_q0292_02.htm";
			}
			else if(cond == 1)
			{
				long reward = st.ownItemCount(GoblinNecklace) * 12 + st.ownItemCount(GoblinPendant) * 36 + st.ownItemCount(GoblinLordPendant) * 33 + st.ownItemCount(SuspiciousContract) * 100;
				if(reward == 0)
					return "elder_spiron_q0292_04.htm";
				if(st.ownItemCount(SuspiciousContract) != 0)
					htmltext = "elder_spiron_q0292_10.htm";
				else if(st.ownItemCount(SuspiciousMemo) == 0)
					htmltext = "elder_spiron_q0292_05.htm";
				else if(st.ownItemCount(SuspiciousMemo) == 1)
					htmltext = "elder_spiron_q0292_08.htm";
				else
					htmltext = "elder_spiron_q0292_09.htm";
				st.takeItems(GoblinNecklace, -1);
				st.takeItems(GoblinPendant, -1);
				st.takeItems(GoblinLordPendant, -1);
				st.takeItems(SuspiciousContract, -1);
				st.giveItems(ADENA_ID, reward);
			}
		}
		else if(npcId == Balanki && cond == 1)
			if(st.ownItemCount(SuspiciousContract) == 0)
				htmltext = "balanki_q0292_01.htm";
			else
			{
				st.takeItems(SuspiciousContract, -1);
				st.giveItems(ADENA_ID, 120);
				htmltext = "balanki_q0292_02.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int i = 0; i < DROPLIST_COND.length; i++)
			if(cond == DROPLIST_COND[i][0] && npcId == DROPLIST_COND[i][2])
				if(DROPLIST_COND[i][3] == 0 || st.ownItemCount(DROPLIST_COND[i][3]) > 0)
					if(DROPLIST_COND[i][5] == 0)
						st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][6]);
					else if(st.rollAndGive(DROPLIST_COND[i][4], DROPLIST_COND[i][7], DROPLIST_COND[i][7], DROPLIST_COND[i][5], DROPLIST_COND[i][6]))
						if(DROPLIST_COND[i][1] != cond && DROPLIST_COND[i][1] != 0)
						{
							st.setCond(Integer.valueOf(DROPLIST_COND[i][1]));
							st.setState(STARTED);
						}
		if(st.ownItemCount(SuspiciousContract) == 0 && Rnd.chance(Chance))
			if(st.ownItemCount(SuspiciousMemo) < 3)
			{
				st.giveItems(SuspiciousMemo, 1);
				st.soundEffect(SOUND_ITEMGET);
			}
			else
			{
				st.takeItems(SuspiciousMemo, -1);
				st.giveItems(SuspiciousContract, 1);
				st.soundEffect(SOUND_MIDDLE);
			}
		return null;
	}
}