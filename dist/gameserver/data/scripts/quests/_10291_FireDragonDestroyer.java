package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */

public class _10291_FireDragonDestroyer extends Quest implements ScriptFile
{
	private static final int Klein = 31540;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int Valakas = 29028;

	public _10291_FireDragonDestroyer()
	{
		super(PARTY_ALL);
		addStartNpc(Klein);
		addQuestItem(PoorNecklace, ValorNecklace);
		addKillId(Valakas);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("klein_q10291_04.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
			st.giveItems(PoorNecklace, 1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Klein)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 83 && st.ownItemCount(7267) >= 1)
					htmltext = "klein_q10291_01.htm";
				else if(st.ownItemCount(7267) < 1)
					htmltext = "klein_q10291_00a.htm";
				else
					htmltext = "klein_q10291_00.htm";
			}
			else if(cond == 1)
				htmltext = "klein_q10291_05.htm";
			else if(cond == 2)
				if(st.ownItemCount(ValorNecklace) >= 1)
				{
					htmltext = "klein_q10291_07.htm";
					st.takeAllItems(ValorNecklace);
					st.giveItems(8567, 1);
					st.giveItems(ADENA_ID, 126549);
					st.addExpAndSp(717291, 77397);
					st.soundEffect(SOUND_FINISH);
					st.setState(COMPLETED);
					st.exitQuest(false);
				}
				else
					htmltext = "klein_q10291_06.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(cond == 1 && npcId == Valakas)
		{
			st.takeAllItems(PoorNecklace);
			st.giveItems(ValorNecklace, 1);
			st.setCond(2);
		}
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