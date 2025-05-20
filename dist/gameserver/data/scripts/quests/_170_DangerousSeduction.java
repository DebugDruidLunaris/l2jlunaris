package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _170_DangerousSeduction extends Quest implements ScriptFile
{
	//NPC
	private static final int Vellior = 30305;
	//Quest Items
	private static final int NightmareCrystal = 1046;
	//MOB
	private static final int Merkenis = 27022;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _170_DangerousSeduction()
	{
		super(false);
		addStartNpc(Vellior);
		addTalkId(Vellior);
		addKillId(Merkenis);
		addQuestItem(NightmareCrystal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30305-04.htm"))
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
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == Vellior)
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.darkelf)
				{
					htmltext = "30305-00.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() < 21)
				{
					htmltext = "30305-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30305-03.htm";
			}
			else if(cond == 1)
				htmltext = "30305-05.htm";
			else if(cond == 2)
			{
				st.takeItems(NightmareCrystal, -1);
				st.giveItems(ADENA_ID, 102680, true);
				st.addExpAndSp(38607, 4018);
				htmltext = "30305-06.htm";
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == Merkenis)
		{
			if(st.ownItemCount(NightmareCrystal) == 0)
				st.giveItems(NightmareCrystal, 1);
			st.soundEffect(SOUND_MIDDLE);
			st.setCond(2);
			st.setState(STARTED);
		}
		return null;
	}
}