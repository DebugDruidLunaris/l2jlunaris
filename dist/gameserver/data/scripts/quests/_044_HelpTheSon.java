package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _044_HelpTheSon extends Quest implements ScriptFile
{
	private static final int LUNDY = 30827;
	private static final int DRIKUS = 30505;

	private static final int WORK_HAMMER = 168;
	private static final int GEMSTONE_FRAGMENT = 7552;
	private static final int GEMSTONE = 7553;
	private static final int PET_TICKET = 7585;

	private static final int MAILLE_GUARD = 20921;
	private static final int MAILLE_SCOUT = 20920;
	private static final int MAILLE_LIZARDMAN = 20919;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _044_HelpTheSon()
	{
		super(false);

		addStartNpc(LUNDY);

		addTalkId(DRIKUS);

		addKillId(MAILLE_GUARD);
		addKillId(MAILLE_SCOUT);
		addKillId(MAILLE_LIZARDMAN);

		addQuestItem(GEMSTONE_FRAGMENT);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "pet_manager_lundy_q0044_0104.htm";
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("3") && st.ownItemCount(WORK_HAMMER) > 0)
		{
			htmltext = "pet_manager_lundy_q0044_0201.htm";
			st.takeItems(WORK_HAMMER, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.ownItemCount(GEMSTONE_FRAGMENT) >= 30)
		{
			htmltext = "pet_manager_lundy_q0044_0301.htm";
			st.takeItems(GEMSTONE_FRAGMENT, -1);
			st.giveItems(GEMSTONE, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.ownItemCount(GEMSTONE) > 0)
		{
			htmltext = "high_prefect_drikus_q0044_0401.htm";
			st.takeItems(GEMSTONE, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_lundy_q0044_0501.htm";
			st.giveItems(PET_TICKET, 1);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int id = st.getState();
		if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 24)
				htmltext = "pet_manager_lundy_q0044_0101.htm";
			else
			{
				st.exitQuest(true);
				htmltext = "pet_manager_lundy_q0044_0103.htm";
			}
		}
		else if(id == STARTED)
		{
			int cond = st.getCond();
			if(npcId == LUNDY)
			{
				if(cond == 1)
				{
					if(st.ownItemCount(WORK_HAMMER) == 0)
						htmltext = "pet_manager_lundy_q0044_0106.htm";
					else
						htmltext = "pet_manager_lundy_q0044_0105.htm";
				}
				else if(cond == 2)
					htmltext = "pet_manager_lundy_q0044_0204.htm";
				else if(cond == 3)
					htmltext = "pet_manager_lundy_q0044_0203.htm";
				else if(cond == 4)
					htmltext = "pet_manager_lundy_q0044_0303.htm";
				else if(cond == 5)
					htmltext = "pet_manager_lundy_q0044_0401.htm";
			}
			else if(npcId == DRIKUS)
				if(cond == 4 && st.ownItemCount(GEMSTONE) > 0)
					htmltext = "high_prefect_drikus_q0044_0301.htm";
				else if(cond == 5)
					htmltext = "high_prefect_drikus_q0044_0403.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2 && st.ownItemCount(GEMSTONE_FRAGMENT) < 30)
		{
			st.giveItems(GEMSTONE_FRAGMENT, 1);
			if(st.ownItemCount(GEMSTONE_FRAGMENT) >= 30)
			{
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(3);
				st.soundEffect(SOUND_ITEMGET);
			}
			else
				st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}