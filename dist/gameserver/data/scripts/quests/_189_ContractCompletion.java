package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _189_ContractCompletion extends Quest implements ScriptFile
{
	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Luka = 30621;
	private static final int Shegfield = 30068;

	private static final int Metal = 10370;

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

	public _189_ContractCompletion()
	{
		super(false);

		addTalkId(Kusto, Luka, Lorain, Shegfield);
		addFirstTalkId(Luka);
		addQuestItem(Metal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("blueprint_seller_luka_q0189_03.htm"))
		{
			st.soundEffect(SOUND_ACCEPT);
			st.setCond(1);
			st.giveItems(Metal, 1);
		}
		else if (event.equalsIgnoreCase("researcher_lorain_q0189_02.htm"))
		{
			st.soundEffect(SOUND_MIDDLE);
			st.setCond(2);
			st.takeItems(Metal, -1);
		}
		else if (event.equalsIgnoreCase("shegfield_q0189_03.htm"))
		{
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("head_blacksmith_kusto_q0189_02.htm"))
		{
			st.giveItems(ADENA_ID, 121527);
			st.addExpAndSp(309467, 20614);
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
			if (npcId == Luka)
			{
				if (cond == 0)
					if (st.getPlayer().getLevel() < 42)
						htmltext = "blueprint_seller_luka_q0189_02.htm";
					else
						htmltext = "blueprint_seller_luka_q0189_01.htm";
				else if (cond == 1)
					htmltext = "blueprint_seller_luka_q0189_04.htm";
			}
			else if (npcId == Lorain)
			{
				if (cond == 1)
					htmltext = "researcher_lorain_q0189_01.htm";
				else if (cond == 2)
					htmltext = "researcher_lorain_q0189_03.htm";
				else if (cond == 3)
				{
					htmltext = "researcher_lorain_q0189_04.htm";
					st.setCond(4);
					st.soundEffect(SOUND_MIDDLE);
				}
				else if (cond == 4)
					htmltext = "researcher_lorain_q0189_05.htm";
			}
			else if (npcId == Shegfield)
			{
				if (cond == 2)
					htmltext = "shegfield_q0189_01.htm";
				else if (cond == 3)
					htmltext = "shegfield_q0189_04.htm";
			}
			else if (npcId == Kusto)
				if (cond == 4)
					htmltext = "head_blacksmith_kusto_q0189_01.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState qs = player.getQuestState(_186_ContractExecution.class);
		if (qs != null && qs.isCompleted() && player.getQuestState(getClass()) == null)
			newQuestState(player, STARTED);
		return "";
	}
}