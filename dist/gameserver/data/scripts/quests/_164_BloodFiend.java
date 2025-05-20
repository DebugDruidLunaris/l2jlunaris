package quests;

import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _164_BloodFiend extends Quest implements ScriptFile
{
	//NPC
	private static final int Creamees = 30149;
	//Quest Items
	private static final int KirunakSkull = 1044;
	//MOB
	private static final int Kirunak = 27021;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _164_BloodFiend()
	{
		super(false);

		addStartNpc(Creamees);
		addTalkId(Creamees);
		addKillId(Kirunak);
		addQuestItem(KirunakSkull);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30149-04.htm"))
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
		if(npcId == Creamees)
			if(cond == 0)
			{
				if(st.getPlayer().getRace() == Race.darkelf)
				{
					htmltext = "30149-00.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() < 21)
				{
					htmltext = "30149-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30149-03.htm";
			}
			else if(cond == 1)
				htmltext = "30149-05.htm";
			else if(cond == 2)
			{
				st.takeItems(KirunakSkull, -1);
				st.giveItems(ADENA_ID, 42130, true);
				st.addExpAndSp(35637, 1854);
				htmltext = "30149-06.htm";
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
		if(cond == 1 && npcId == Kirunak)
		{
			if(st.ownItemCount(KirunakSkull) == 0)
				st.giveItems(KirunakSkull, 1);
			st.soundEffect(SOUND_MIDDLE);
			st.setCond(2);
			st.setState(STARTED);
		}
		return null;
	}
}