package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * Квест Vanquish Remnants
 *
 * @author Sergey Ibryaev aka Artful
 */

public class _326_VanquishRemnants extends Quest implements ScriptFile
{
	//NPC
	private static final int Leopold = 30435;
	//Quest Items
	private static final int RedCrossBadge = 1359;
	private static final int BlueCrossBadge = 1360;
	private static final int BlackCrossBadge = 1361;
	//Items
	private static final int BlackLionMark = 1369;
	//MOB
	private static final int OlMahumPatrol = 30425;
	private static final int OlMahumGuard = 20058;
	private static final int OlMahumStraggler = 20061;
	private static final int OlMahumShooter = 20063;
	private static final int OlMahumCaptain = 20066;
	private static final int OlMahumCommander = 20076;
	private static final int OlMahumSupplier = 20436;
	private static final int OlMahumRecruit = 20437;
	private static final int OlMahumGeneral = 20438;
	//Drop Cond
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	public final int[][] DROPLIST_COND = {
			{ OlMahumPatrol, RedCrossBadge },
			{ OlMahumGuard, RedCrossBadge },
			{ OlMahumRecruit, RedCrossBadge },
			{ OlMahumStraggler, BlueCrossBadge },
			{ OlMahumShooter, BlueCrossBadge },
			{ OlMahumSupplier, BlueCrossBadge },
			{ OlMahumCaptain, BlackCrossBadge },
			{ OlMahumGeneral, BlackCrossBadge },
			{ OlMahumCommander, BlackCrossBadge } };

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _326_VanquishRemnants()
	{
		super(false);
		addStartNpc(Leopold);
		addTalkId(Leopold);
		//Mob Drop
		for(int i = 0; i < DROPLIST_COND.length; i++)
			addKillId(DROPLIST_COND[i][0]);
		addQuestItem(RedCrossBadge);
		addQuestItem(BlueCrossBadge);
		addQuestItem(BlackCrossBadge);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("leopold_q0326_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("leopold_q0326_03.htm"))
		{
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == Leopold)
			if(st.getPlayer().getLevel() < 21)
			{
				htmltext = "leopold_q0326_01.htm";
				st.exitQuest(true);
			}
			else if(cond == 0)
				htmltext = "leopold_q0326_02.htm";
			else if(cond == 1 && st.ownItemCount(RedCrossBadge) == 0 && st.ownItemCount(BlueCrossBadge) == 0 && st.ownItemCount(BlackCrossBadge) == 0)
				htmltext = "leopold_q0326_04.htm";
			else if(cond == 1)
			{
				if(st.ownItemCount(RedCrossBadge) + st.ownItemCount(BlueCrossBadge) + st.ownItemCount(BlackCrossBadge) >= 100)
				{
					if(st.ownItemCount(BlackLionMark) == 0)
					{
						htmltext = "leopold_q0326_09.htm";
						st.giveItems(BlackLionMark, 1);
					}
					else
						htmltext = "leopold_q0326_06.htm";
				}
				else
					htmltext = "leopold_q0326_05.htm";
				st.giveItems(ADENA_ID, st.ownItemCount(RedCrossBadge) * 89 + st.ownItemCount(BlueCrossBadge) * 95 + st.ownItemCount(BlackCrossBadge) * 101, true);
				st.takeItems(RedCrossBadge, -1);
				st.takeItems(BlueCrossBadge, -1);
				st.takeItems(BlackCrossBadge, -1);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getState() == STARTED)
			for(int i = 0; i < DROPLIST_COND.length; i++)
				if(npc.getNpcId() == DROPLIST_COND[i][0])
					st.giveItems(DROPLIST_COND[i][1], 1);
		return null;
	}
}