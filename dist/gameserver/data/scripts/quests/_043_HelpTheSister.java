package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _043_HelpTheSister extends Quest implements ScriptFile
{
	private static final int COOPER = 30829;
	private static final int GALLADUCCI = 30097;

	private static final int CRAFTED_DAGGER = 220;
	private static final int MAP_PIECE = 7550;
	private static final int MAP = 7551;
	private static final int PET_TICKET = 7584;

	private static final int SPECTER = 20171;
	private static final int SORROW_MAIDEN = 20197;

	private static final int MAX_COUNT = 30;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _043_HelpTheSister()
	{
		super(false);

		addStartNpc(COOPER);

		addTalkId(GALLADUCCI);

		addKillId(SPECTER);
		addKillId(SORROW_MAIDEN);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			htmltext = "pet_manager_cooper_q0043_0104.htm";
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("3") && st.ownItemCount(CRAFTED_DAGGER) > 0)
		{
			htmltext = "pet_manager_cooper_q0043_0201.htm";
			st.takeItems(CRAFTED_DAGGER, 1);
			st.setCond(2);
		}
		else if(event.equals("4") && st.ownItemCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "pet_manager_cooper_q0043_0301.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.setCond(4);
		}
		else if(event.equals("5") && st.ownItemCount(MAP) > 0)
		{
			htmltext = "galladuchi_q0043_0401.htm";
			st.takeItems(MAP, 1);
			st.setCond(5);
		}
		else if(event.equals("7"))
		{
			htmltext = "pet_manager_cooper_q0043_0501.htm";
			st.giveItems(PET_TICKET, 1);
			st.setCond(0);
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
			if(st.getPlayer().getLevel() >= 26)
				htmltext = "pet_manager_cooper_q0043_0101.htm";
			else
			{
				st.exitQuest(true);
				htmltext = "pet_manager_cooper_q0043_0103.htm";
			}
		}
		else if(id == STARTED)
		{
			int cond = st.getCond();
			if(npcId == COOPER)
			{
				if(cond == 1)
				{
					if(st.ownItemCount(CRAFTED_DAGGER) == 0)
						htmltext = "pet_manager_cooper_q0043_0106.htm";
					else
						htmltext = "pet_manager_cooper_q0043_0105.htm";
				}
				else if(cond == 2)
					htmltext = "pet_manager_cooper_q0043_0204.htm";
				else if(cond == 3)
					htmltext = "pet_manager_cooper_q0043_0203.htm";
				else if(cond == 4)
					htmltext = "pet_manager_cooper_q0043_0303.htm";
				else if(cond == 5)
					htmltext = "pet_manager_cooper_q0043_0401.htm";
			}
			else if(npcId == GALLADUCCI)
				if(cond == 4 && st.ownItemCount(MAP) > 0)
					htmltext = "galladuchi_q0043_0301.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 2)
		{
			long pieces = st.ownItemCount(MAP_PIECE);
			if(pieces < MAX_COUNT)
			{
				st.giveItems(MAP_PIECE, 1);
				if(pieces < MAX_COUNT - 1)
					st.soundEffect(SOUND_ITEMGET);
				else
				{
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(3);
				}
			}
		}
		return null;
	}
}