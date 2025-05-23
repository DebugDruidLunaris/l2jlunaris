package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _694_BreakThroughTheHallOfSuffering extends Quest implements ScriptFile
{
	// NPC
	private static final int TEPIOS = 32603;

	// Item rewards
	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int MARK_OF_KEUCEREUS_STAGE_2 = 13692;

	public _694_BreakThroughTheHallOfSuffering()
	{
		super(PARTY_ALL);
		addStartNpc(TEPIOS);
	}

	@Override
	public String onEvent(String event, QuestState qs, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("32603-04.htm"))
		{
			qs.setCond(1);
			qs.setState(STARTED);
			qs.soundEffect(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Player player = st.getPlayer();

		if(npcId == TEPIOS)
			if(cond == 0)
			{
				if(player.getLevel() < 75 || player.getLevel() > 82)
				{
					if(st.ownItemCount(MARK_OF_KEUCEREUS_STAGE_1) == 0 && st.ownItemCount(MARK_OF_KEUCEREUS_STAGE_2) == 0 && player.getLevel() > 82)
						st.giveItems(MARK_OF_KEUCEREUS_STAGE_1, 1);
					st.exitQuest(true);
					htmltext = "32603-00.htm";
				}
				else
					htmltext = "32603-01.htm";
			}
			else if(cond == 1)
				htmltext = "32603-05.htm";
		return htmltext;
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