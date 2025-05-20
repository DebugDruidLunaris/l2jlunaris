package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _366_SilverHairedShaman extends Quest implements ScriptFile
{
	//NPC
	private static final int DIETER = 30111;

	//MOBS
	private static final int SAIRON = 20986;
	private static final int SAIRONS_DOLL = 20987;
	private static final int SAIRONS_PUPPET = 20988;
	//VARIABLES
	private static final int ADENA_PER_ONE = 500;
	private static final int START_ADENA = 12070;

	//QUEST ITEMS
	private static final int SAIRONS_SILVER_HAIR = 5874;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _366_SilverHairedShaman()
	{
		super(false);
		addStartNpc(DIETER);

		addKillId(SAIRON);
		addKillId(SAIRONS_DOLL);
		addKillId(SAIRONS_PUPPET);

		addQuestItem(SAIRONS_SILVER_HAIR);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30111-02.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30111-quit.htm"))
		{
			st.takeItems(SAIRONS_SILVER_HAIR, -1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();
		if(id == CREATED)
			st.setCond(0);
		else
			cond = st.getCond();
		if(npcId == 30111)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 48)
					htmltext = "30111-01.htm";
				else
				{
					htmltext = "30111-00.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 && st.ownItemCount(SAIRONS_SILVER_HAIR) == 0)
				htmltext = "30111-03.htm";
			else if(cond == 1 && st.ownItemCount(SAIRONS_SILVER_HAIR) >= 1)
			{
				st.giveItems(ADENA_ID, st.ownItemCount(SAIRONS_SILVER_HAIR) * ADENA_PER_ONE + START_ADENA);
				st.takeItems(SAIRONS_SILVER_HAIR, -1);
				htmltext = "30111-have.htm";
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1 && Rnd.chance(66))
		{
			st.giveItems(SAIRONS_SILVER_HAIR, 1);
			st.soundEffect(SOUND_MIDDLE);
		}
		return null;
	}
}