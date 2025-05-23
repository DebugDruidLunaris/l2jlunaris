package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _190_LostDream extends Quest implements ScriptFile
{
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;
	private static final int Juris = 30113;

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}

	public _190_LostDream()
	{
		super(false);

		addTalkId(Kusto, Nikola, Lorain, Juris);
		addFirstTalkId(Kusto);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("head_blacksmith_kusto_q0190_03.htm"))
		{
			st.soundEffect(SOUND_ACCEPT);
			st.setCond(1);
		}
		else if (event.equalsIgnoreCase("head_blacksmith_kusto_q0190_06.htm"))
		{
			st.soundEffect(SOUND_MIDDLE);
			st.setCond(3);
		}
		else if (event.equalsIgnoreCase("juria_q0190_03.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if (st.getState() == STARTED)
			if (npcId == Kusto)
			{
				if (cond == 0)
					if (st.getPlayer().getLevel() < 42)
						htmltext = "head_blacksmith_kusto_q0190_02.htm";
					else
						htmltext = "head_blacksmith_kusto_q0190_01.htm";
				else if (cond == 1)
					htmltext = "head_blacksmith_kusto_q0190_04.htm";
				else if (cond == 2)
					htmltext = "head_blacksmith_kusto_q0190_05.htm";
				else if (cond == 3)
					htmltext = "head_blacksmith_kusto_q0190_07.htm";
				else if (cond == 5)
				{
					htmltext = "head_blacksmith_kusto_q0190_08.htm";
					st.giveItems(ADENA_ID, 109427);
					st.addExpAndSp(309467, 20614);
					st.exitQuest(false);
					st.soundEffect(SOUND_FINISH);
				}
			}
			else if (npcId == Juris)
			{
				if (cond == 1)
					htmltext = "juria_q0190_01.htm";
				else if (cond == 2)
					htmltext = "juria_q0190_04.htm";
			}
			else if (npcId == Lorain)
			{
				if (cond == 3)
				{
					htmltext = "researcher_lorain_q0190_01.htm";
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(4);
				}
				else if (cond == 4)
					htmltext = "researcher_lorain_q0190_02.htm";
			}
			else if (npcId == Nikola)
				if (cond == 4)
				{
					htmltext = "maestro_nikola_q0190_01.htm";
					st.soundEffect(SOUND_MIDDLE);
					st.setCond(5);
				}
				else if (cond == 5)
					htmltext = "maestro_nikola_q0190_02.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState qs = player.getQuestState(_187_NikolasHeart.class);
		if (qs != null && qs.isCompleted() && player.getQuestState(getClass()) == null)
			newQuestState(player, STARTED);
		return "";
	}
}