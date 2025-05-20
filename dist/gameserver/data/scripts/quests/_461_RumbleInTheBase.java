package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author pchayka
 */

public class _461_RumbleInTheBase extends Quest implements ScriptFile
{
	private static final int Stan = 30200;
	private static final int ShoesStringofSelMahum = 16382;
	private static final int ShinySalmon = 15503;
	private static final int[] SelMahums = { 22786, 22787, 22788 };
	private static final int SelChef = 18908;

	public _461_RumbleInTheBase()
	{
		super(false);
		addStartNpc(Stan);
		addQuestItem(ShoesStringofSelMahum, ShinySalmon);
		addKillId(SelMahums);
		addKillId(SelChef);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("stan_q461_03.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npc.getNpcId() == Stan)
			switch(st.getState())
			{
				case CREATED:
					QuestState qs = st.getPlayer().getQuestState(_252_ItSmellsDelicious.class);
					if(st.getPlayer().getLevel() >= 82 && qs != null && qs.isCompleted())
					{
						if(st.isDailyQuest())
							htmltext = "stan_q461_01.htm";
						else
							htmltext = "stan_q461_00a.htm";
					}
					else
						htmltext = "stan_q461_00.htm";
					break;
				case STARTED:
					if(cond == 1)
						htmltext = "stan_q461_04.htm";
					else if(cond == 2)
					{
						htmltext = "stan_q461_05.htm";
						st.takeAllItems(ShoesStringofSelMahum);
						st.takeAllItems(ShinySalmon);
						st.addExpAndSp(224784, 342528);
						st.setState(COMPLETED);
						st.soundEffect(SOUND_FINISH);
						st.exitQuest(this);
					}
					break;
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		if(cond == 1)
			if(st.ownItemCount(ShoesStringofSelMahum) < 10 && st.ownItemCount(ShinySalmon) < 5)
			{
				if(st.ownItemCount(ShoesStringofSelMahum) < 10 && ArrayUtils.contains(SelMahums, npcId))
					st.rollAndGive(ShoesStringofSelMahum, 1, 20);
				if(st.ownItemCount(ShinySalmon) < 5 && npcId == SelChef)
					st.rollAndGive(ShinySalmon, 1, 10);
			}
			else
				st.setCond(2);
		return null;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}