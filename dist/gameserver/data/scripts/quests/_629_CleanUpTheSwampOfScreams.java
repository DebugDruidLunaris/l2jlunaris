package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _629_CleanUpTheSwampOfScreams extends Quest implements ScriptFile
{
	//NPC
	private static int CAPTAIN = 31553;
	private static int CLAWS = 7250;
	private static int COIN = 7251;

	//CHANCES
	private static int[][] CHANCE = {
			{ 21508, 50 },
			{ 21509, 43 },
			{ 21510, 52 },
			{ 21511, 57 },
			{ 21512, 74 },
			{ 21513, 53 },
			{ 21514, 53 },
			{ 21515, 54 },
			{ 21516, 55 },
			{ 21517, 56 } };

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _629_CleanUpTheSwampOfScreams()
	{
		super(false);

		addStartNpc(CAPTAIN);

		for(int npcId = 21508; npcId < 21518; npcId++)
			addKillId(npcId);

		addQuestItem(CLAWS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("merc_cap_peace_q0629_0104.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0629_0202.htm"))
		{
			if(st.ownItemCount(CLAWS) >= 100)
			{
				st.takeItems(CLAWS, 100);
				st.giveItems(COIN, 20, false);
			}
			else
				htmltext = "merc_cap_peace_q0629_0203.htm";
		}
		else if(event.equalsIgnoreCase("merc_cap_peace_q0629_0204.htm"))
		{
			st.takeItems(CLAWS, -1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(st.ownItemCount(7246) > 0 || st.ownItemCount(7247) > 0)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 66)
					htmltext = "merc_cap_peace_q0629_0101.htm";
				else
				{
					htmltext = "merc_cap_peace_q0629_0103.htm";
					st.exitQuest(true);
				}
			}
			else if(st.getState() == STARTED)
				if(st.ownItemCount(CLAWS) >= 100)
					htmltext = "merc_cap_peace_q0629_0105.htm";
				else
					htmltext = "merc_cap_peace_q0629_0106.htm";
		}
		else
		{
			htmltext = "merc_cap_peace_q0629_0205.htm";
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() == STARTED)
			st.rollAndGive(CLAWS, 1, CHANCE[npc.getNpcId() - 21508][1]);
		return null;
	}
}