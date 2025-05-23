package quests;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */
public class _696_ConquertheHallofErosion extends Quest implements ScriptFile
{
	// NPC
	private static final int TEPIOS = 32603;
	private static final int Cohemenes = 25634;

	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int MARK_OF_KEUCEREUS_STAGE_2 = 13692;

	public _696_ConquertheHallofErosion()
	{
		super(PARTY_ALL);
		addStartNpc(TEPIOS);
		addKillId(Cohemenes);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("tepios_q696_3.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		Player player = st.getPlayer();
		int cond = st.getCond();

		if(npcId == TEPIOS)
			if(cond == 0)
			{
				if(player.getLevel() >= 75)
				{
					if(st.ownItemCount(MARK_OF_KEUCEREUS_STAGE_1) > 0 || st.ownItemCount(MARK_OF_KEUCEREUS_STAGE_2) > 0)
						htmltext = "tepios_q696_1.htm";
					else
					{
						htmltext = "tepios_q696_6.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "tepios_q696_0.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				if(st.getInt("cohemenesDone") != 0)
				{
					if(st.ownItemCount(MARK_OF_KEUCEREUS_STAGE_2) < 1)
					{
						st.takeAllItems(MARK_OF_KEUCEREUS_STAGE_1);
						st.giveItems(MARK_OF_KEUCEREUS_STAGE_2, 1);
					}
					htmltext = "tepios_q696_5.htm";
					st.soundEffect(SOUND_FINISH);
					st.exitQuest(true);
				}
				else
					htmltext = "tepios_q696_1a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(npc.getNpcId() == Cohemenes)
			st.setMemoState("cohemenesDone", 1);
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