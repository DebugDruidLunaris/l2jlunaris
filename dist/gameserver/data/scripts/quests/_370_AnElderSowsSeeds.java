package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _370_AnElderSowsSeeds extends Quest implements ScriptFile
{
	//npc
	private static int CASIAN = 30612;
	//mobs
	private static int[] MOBS = { 20082, 20084, 20086, 20089, 20090 };
	//items
	private static int SPB_PAGE = 5916;
	//Collection Kranvel's Spellbooks
	private static int[] CHAPTERS = { 5917, 5918, 5919, 5920 };

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _370_AnElderSowsSeeds()
	{
		super(false);

		addStartNpc(CASIAN);

		for(int npcId : MOBS)
			addKillId(npcId);

		addQuestItem(SPB_PAGE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;

		if(event.equalsIgnoreCase("30612-1.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30612-6.htm"))
		{
			if(st.ownItemCount(CHAPTERS[0]) > 0 && st.ownItemCount(CHAPTERS[1]) > 0 && st.ownItemCount(CHAPTERS[2]) > 0 && st.ownItemCount(CHAPTERS[3]) > 0)
			{
				long mincount = st.ownItemCount(CHAPTERS[0]);

				for(int itemId : CHAPTERS)
					mincount = Math.min(mincount, st.ownItemCount(itemId));

				for(int itemId : CHAPTERS)
					st.takeItems(itemId, mincount);

				st.giveItems(ADENA_ID, 3600 * mincount);
				htmltext = "30612-8.htm";
			}
			else
				htmltext = "30612-4.htm";
		}
		else if(event.equalsIgnoreCase("30612-9.htm"))
		{
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";

		int cond = st.getCond();

		if(st.getState() == CREATED)
		{
			if(st.getPlayer().getLevel() < 28)
			{
				htmltext = "30612-0a.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "30612-0.htm";
		}
		else if(cond == 1)
			htmltext = "30612-4.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;

		if(Rnd.chance(Math.min((int) (15 * st.getRateQuestsReward()), 100)))
		{
			st.giveItems(SPB_PAGE, 1);
			st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}