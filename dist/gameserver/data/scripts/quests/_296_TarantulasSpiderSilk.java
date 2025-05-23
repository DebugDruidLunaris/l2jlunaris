package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

public class _296_TarantulasSpiderSilk extends Quest implements ScriptFile
{
	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	private static final int TARANTULA_SPIDER_SILK = 1493;
	private static final int TARANTULA_SPINNERETTE = 1494;
	private static final int RING_OF_RACCOON = 1508;
	private static final int RING_OF_FIREFLY = 1509;

	public _296_TarantulasSpiderSilk()
	{
		super(false);
		addStartNpc(30519);
		addTalkId(30548);

		addKillId(20394);
		addKillId(20403);
		addKillId(20508);

		addQuestItem(TARANTULA_SPIDER_SILK);
		addQuestItem(TARANTULA_SPINNERETTE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("trader_mion_q0296_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "trader_mion_q0296_06.htm";
			st.takeItems(TARANTULA_SPINNERETTE, -1);
			st.exitQuest(true);
			st.soundEffect(SOUND_FINISH);
		}
		else if(event.equalsIgnoreCase("exchange"))
			if(st.ownItemCount(TARANTULA_SPINNERETTE) >= 1)
			{
				htmltext = "defender_nathan_q0296_03.htm";
				st.giveItems(TARANTULA_SPIDER_SILK, 17);
				st.takeItems(TARANTULA_SPINNERETTE, -1);
			}
			else
				htmltext = "defender_nathan_q0296_02.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == 30519)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 15)
				{
					if(st.ownItemCount(RING_OF_RACCOON) > 0 || st.ownItemCount(RING_OF_FIREFLY) > 0)
						htmltext = "trader_mion_q0296_02.htm";
					else
					{
						htmltext = "trader_mion_q0296_08.htm";
						return htmltext;
					}
				}
				else
				{
					htmltext = "trader_mion_q0296_01.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				if(st.ownItemCount(TARANTULA_SPIDER_SILK) < 1)
					htmltext = "trader_mion_q0296_04.htm";
				else if(st.ownItemCount(TARANTULA_SPIDER_SILK) >= 1)
				{
					htmltext = "trader_mion_q0296_05.htm";
					st.giveItems(ADENA_ID, st.ownItemCount(TARANTULA_SPIDER_SILK) * 23);
					st.takeItems(TARANTULA_SPIDER_SILK, -1);

					if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
					{
						st.getPlayer().setVar("p1q4", "1", -1);
						st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
}
				}
		}
		else if(npcId == 30548 && cond == 1)
			htmltext = "defender_nathan_q0296_01.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
			if(Rnd.chance(50))
				st.rollAndGive(TARANTULA_SPINNERETTE, 1, 45);
			else
				st.rollAndGive(TARANTULA_SPIDER_SILK, 1, 45);
		return null;
	}
}