package quests;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.instancemanager.SoIManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */

public class _698_BlocktheLordsEscape extends Quest implements ScriptFile
{
	// NPC
	private static final int TEPIOS = 32603;
	private static final int VesperNobleEnhanceStone = 14052;

	public _698_BlocktheLordsEscape()
	{
		super(PARTY_ALL);
		addStartNpc(TEPIOS);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		Player player = st.getPlayer();

		if(npcId == TEPIOS)
			if(st.getState() == CREATED)
			{
				if(player.getLevel() < 75 || player.getLevel() > 85)
				{
					st.exitQuest(true);
					return "tepios_q698_0.htm";
				}
				if(SoIManager.getCurrentStage() != 5)
				{
					st.exitQuest(true);
					return "tepios_q698_0a.htm";
				}
				return "tepios_q698_1.htm";
			}
			else if(st.getCond() == 1 && st.getInt("defenceDone") == 1)
			{
				htmltext = "tepios_q698_5.htm";
				st.giveItems(VesperNobleEnhanceStone, (int) Config.RATE_QUESTS_REWARD * Rnd.get(5, 8));
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				return "tepios_q698_4.htm";
		return htmltext;
	}

	@Override
	@SuppressWarnings("unused")
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		String htmltext = event;
		int cond = st.getCond();

		if(event.equalsIgnoreCase("tepios_q698_3.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
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