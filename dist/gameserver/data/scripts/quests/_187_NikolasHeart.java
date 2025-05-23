package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _187_NikolasHeart extends Quest implements ScriptFile
{
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;

	private static final int Certificate = 10362;
	private static final int Metal = 10368;

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

	public _187_NikolasHeart()
	{
		super(false);

		addTalkId(Kusto, Nikola, Lorain);
		addFirstTalkId(Lorain);
		addQuestItem(Certificate, Metal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("researcher_lorain_q0187_03.htm"))
		{
			st.soundEffect(SOUND_ACCEPT);
			st.setCond(1);
			st.takeItems(Certificate, -1);
			st.giveItems(Metal, 1);
		}
		else if (event.equalsIgnoreCase("maestro_nikola_q0187_03.htm"))
		{
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("head_blacksmith_kusto_q0187_03.htm"))
		{
			st.giveItems(ADENA_ID, 93383);
			st.addExpAndSp(285935, 18711);
			st.exitQuest(false);
			st.soundEffect(SOUND_FINISH);
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
			if (npcId == Lorain)
			{
				if (cond == 0)
					if (st.getPlayer().getLevel() < 41)
						htmltext = "researcher_lorain_q0187_02.htm";
					else
						htmltext = "researcher_lorain_q0187_01.htm";
				else if (cond == 1)
					htmltext = "researcher_lorain_q0187_04.htm";
			}
			else if (npcId == Nikola)
			{
				if (cond == 1)
					htmltext = "maestro_nikola_q0187_01.htm";
				else if (cond == 2)
					htmltext = "maestro_nikola_q0187_04.htm";
			}
			else if (npcId == Kusto)
				if (cond == 2)
					htmltext = "head_blacksmith_kusto_q0187_01.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState qs = player.getQuestState(_185_NikolasCooperationConsideration.class);
		if (qs != null && qs.isCompleted() && player.getQuestState(getClass()) == null)
			newQuestState(player, STARTED);
		return "";
	}
}