package quests;

import org.apache.commons.lang3.ArrayUtils;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _252_ItSmellsDelicious extends Quest implements ScriptFile
{
	private static final int GuardStan = 30200;
	private static final int[] SelMahums = {
			22786,
			22787,
			22788
	};
	private static final int SelChef = 18908;
	private static final int SelMahumDiary = 15500;
	private static final int SelMahumCookbookPage = 15501;

	public _252_ItSmellsDelicious()
	{
		super(false);
		addStartNpc(GuardStan);
		addKillId(SelMahums[0], SelMahums[1], SelMahums[2], SelChef);
		addQuestItem(SelMahumDiary, SelMahumCookbookPage);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("stan_q252_03.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("stan_q252_06.htm"))
		{
			st.takeAllItems(SelMahumDiary, SelMahumCookbookPage);
			st.setState(COMPLETED);
			st.giveItems(57, 147656);
			st.addExpAndSp(716238, 78324);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npc.getNpcId() == GuardStan)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "stan_q252_01.htm";
				else
					htmltext = "stan_q252_00.htm";
			}
			else if(cond == 1)
				htmltext = "stan_q252_04.htm";
			else if(cond == 2)
				htmltext = "stan_q252_05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		if(cond == 1)
		{
			if(st.getQuestItemsCount(SelMahumDiary) < 10 && ArrayUtils.contains(SelMahums, npc.getNpcId()))
				st.rollAndGive(SelMahumDiary, 1, 15);
			if(st.getQuestItemsCount(SelMahumCookbookPage) < 5 && npc.getNpcId() == SelChef)
				st.rollAndGive(SelMahumCookbookPage, 1, 10);
			if(st.getQuestItemsCount(SelMahumDiary) >= 10 && st.getQuestItemsCount(SelMahumCookbookPage) >= 5)
				st.setCond(2);
		}
		return null;
	}

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
}