package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

public class _108_JumbleTumbleDiamondFuss extends Quest implements ScriptFile
{
	int GOUPHS_CONTRACT = 1559;
	int REEPS_CONTRACT = 1560;
	int ELVEN_WINE = 1561;
	int BRONPS_DICE = 1562;
	int BRONPS_CONTRACT = 1563;
	int AQUAMARINE = 1564;
	int CHRYSOBERYL = 1565;
	int GEM_BOX1 = 1566;
	int COAL_PIECE = 1567;
	int BRONPS_LETTER = 1568;
	int BERRY_TART = 1569;
	int BAT_DIAGRAM = 1570;
	int STAR_DIAMOND = 1571;
	int SILVERSMITH_HAMMER = 1511;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _108_JumbleTumbleDiamondFuss()
	{
		super(false);

		addStartNpc(30523);

		addTalkId(30516);
		addTalkId(30521);
		addTalkId(30522);
		addTalkId(30526);
		addTalkId(30529);
		addTalkId(30555);

		addKillId(20323);
		addKillId(20324);
		addKillId(20480);

		addQuestItem(GEM_BOX1, STAR_DIAMOND, GOUPHS_CONTRACT, REEPS_CONTRACT, ELVEN_WINE, BRONPS_CONTRACT, AQUAMARINE, CHRYSOBERYL, COAL_PIECE, BRONPS_DICE, BRONPS_LETTER, BERRY_TART, BAT_DIAGRAM);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("collector_gouph_q0108_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.giveItems(GOUPHS_CONTRACT, 1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("carrier_torocco_q0108_02.htm"))
		{
			st.takeItems(REEPS_CONTRACT, 1);
			st.giveItems(ELVEN_WINE, 1);
			st.setCond(3);
		}
		else if(event.equals("blacksmith_bronp_q0108_02.htm"))
		{
			st.takeItems(BRONPS_DICE, 1);
			st.giveItems(BRONPS_CONTRACT, 1);
			st.setCond(5);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30523)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.dwarf)
				{
					htmltext = "collector_gouph_q0108_00.htm";
					st.exitQuest(true);
				}
				else if(st.getPlayer().getLevel() >= 10)
					htmltext = "collector_gouph_q0108_02.htm";
				else
				{
					htmltext = "collector_gouph_q0108_01.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 0 && st.ownItemCount(GOUPHS_CONTRACT) > 0)
				htmltext = "collector_gouph_q0108_04.htm";
			else if(cond > 1 && cond < 7 && (st.ownItemCount(REEPS_CONTRACT) > 0 || st.ownItemCount(ELVEN_WINE) > 0 || st.ownItemCount(BRONPS_DICE) > 0 || st.ownItemCount(BRONPS_CONTRACT) > 0))
				htmltext = "collector_gouph_q0108_05.htm";
			else if(cond == 7 && st.ownItemCount(GEM_BOX1) > 0)
			{
				htmltext = "collector_gouph_q0108_06.htm";
				st.takeItems(GEM_BOX1, 1);
				st.giveItems(COAL_PIECE, 1);
				st.setCond(8);
			}
			else if(cond > 7 && cond < 12 && (st.ownItemCount(BRONPS_LETTER) > 0 || st.ownItemCount(COAL_PIECE) > 0 || st.ownItemCount(BERRY_TART) > 0 || st.ownItemCount(BAT_DIAGRAM) > 0))
				htmltext = "collector_gouph_q0108_07.htm";
			else if(cond == 12 && st.ownItemCount(STAR_DIAMOND) > 0)
			{
				htmltext = "collector_gouph_q0108_08.htm";
				st.takeItems(STAR_DIAMOND, 1);

				st.giveItems(SILVERSMITH_HAMMER, 1);
				st.getPlayer().addExpAndSp(34565, 2962);
				st.giveItems(ADENA_ID, 14666, false);

				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3"))
				{
					st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
					st.giveItems(1060, 100); // healing potion
					for(int item = 4412; item <= 4417; item++)
						st.giveItems(item, 10); // echo cry
					st.playTutorialVoice("tutorial_voice_026");
					st.giveItems(5789, 6000); // newbie ss
				}

				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if(npcId == 30516)
		{
			if(cond == 1 && st.ownItemCount(GOUPHS_CONTRACT) > 0)
			{
				htmltext = "trader_reep_q0108_01.htm";
				st.giveItems(REEPS_CONTRACT, 1);
				st.takeItems(GOUPHS_CONTRACT, 1);
				st.setCond(2);
			}
			else if(cond >= 2)
				htmltext = "trader_reep_q0108_02.htm";
		}
		else if(npcId == 30555)
		{
			if(cond == 2 && st.ownItemCount(REEPS_CONTRACT) == 1)
				htmltext = "carrier_torocco_q0108_01.htm";
			else if(cond == 3 && st.ownItemCount(ELVEN_WINE) > 0)
				htmltext = "carrier_torocco_q0108_03.htm";
			else if(cond == 7 && st.ownItemCount(GEM_BOX1) == 1)
				htmltext = "carrier_torocco_q0108_04.htm";
			else
				htmltext = "carrier_torocco_q0108_05.htm";
		}
		else if(npcId == 30529)
		{
			if(cond == 3 && st.ownItemCount(ELVEN_WINE) > 0)
			{
				st.takeItems(ELVEN_WINE, 1);
				st.giveItems(BRONPS_DICE, 1);
				htmltext = "miner_maron_q0108_01.htm";
				st.setCond(4);
			}
			else if(cond == 4)
				htmltext = "miner_maron_q0108_02.htm";
			else
				htmltext = "miner_maron_q0108_03.htm";
		}
		else if(npcId == 30526)
		{
			if(cond == 4 && st.ownItemCount(BRONPS_DICE) > 0)
				htmltext = "blacksmith_bronp_q0108_01.htm";
			else if(cond == 5 && st.ownItemCount(BRONPS_CONTRACT) > 0 && (st.ownItemCount(AQUAMARINE) < 10 || st.ownItemCount(CHRYSOBERYL) < 10))
				htmltext = "blacksmith_bronp_q0108_03.htm";
			else if(cond == 6 && st.ownItemCount(BRONPS_CONTRACT) > 0 && st.ownItemCount(AQUAMARINE) == 10 && st.ownItemCount(CHRYSOBERYL) == 10)
			{
				htmltext = "blacksmith_bronp_q0108_04.htm";
				st.takeItems(BRONPS_CONTRACT, -1);
				st.takeItems(AQUAMARINE, -1);
				st.takeItems(CHRYSOBERYL, -1);
				st.giveItems(GEM_BOX1, 1);
				st.setCond(7);
			}
			else if(cond == 7 && st.ownItemCount(GEM_BOX1) > 0)
				htmltext = "blacksmith_bronp_q0108_05.htm";
			else if(cond == 8 && st.ownItemCount(COAL_PIECE) > 0)
			{
				htmltext = "blacksmith_bronp_q0108_06.htm";
				st.takeItems(COAL_PIECE, 1);
				st.giveItems(BRONPS_LETTER, 1);
				st.setCond(9);
			}
			else if(cond == 9 && st.ownItemCount(BRONPS_LETTER) > 0)
				htmltext = "blacksmith_bronp_q0108_07.htm";
			else
				htmltext = "blacksmith_bronp_q0108_08.htm";
		}
		else if(npcId == 30521)
		{
			if(cond == 9 && st.ownItemCount(BRONPS_LETTER) > 0)
			{
				htmltext = "warehouse_murphrin_q0108_01.htm";
				st.takeItems(BRONPS_LETTER, 1);
				st.giveItems(BERRY_TART, 1);
				st.setCond(10);
			}
			else if(cond == 10 && st.ownItemCount(BERRY_TART) > 0)
				htmltext = "warehouse_murphrin_q0108_02.htm";
			else
				htmltext = "warehouse_murphrin_q0108_03.htm";
		}
		else if(npcId == 30522)
			if(cond == 10 && st.ownItemCount(BERRY_TART) > 0)
			{
				htmltext = "warehouse_airy_q0108_01.htm";
				st.takeItems(BERRY_TART, 1);
				st.giveItems(BAT_DIAGRAM, 1);
				st.setCond(11);
			}
			else if(cond == 11 && st.ownItemCount(BAT_DIAGRAM) > 0)
				htmltext = "warehouse_airy_q0108_02.htm";
			else if(cond == 12 && st.ownItemCount(STAR_DIAMOND) > 0)
				htmltext = "warehouse_airy_q0108_03.htm";
			else
				htmltext = "warehouse_airy_q0108_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 20323 || npcId == 20324)
		{
			if(cond == 5 && st.ownItemCount(BRONPS_CONTRACT) > 0)
			{
				if(st.ownItemCount(AQUAMARINE) < 10 && Rnd.chance(80))
				{
					st.giveItems(AQUAMARINE, 1);
					if(st.ownItemCount(AQUAMARINE) < 10)
						st.soundEffect(SOUND_ITEMGET);
					else
					{
						st.soundEffect(SOUND_MIDDLE);
						if(st.ownItemCount(AQUAMARINE) == 10 && st.ownItemCount(CHRYSOBERYL) == 10)
							st.setCond(6);
					}
				}
				if(st.ownItemCount(CHRYSOBERYL) < 10 && Rnd.chance(80))
				{
					st.giveItems(CHRYSOBERYL, 1);
					if(st.ownItemCount(CHRYSOBERYL) < 10)
						st.soundEffect(SOUND_ITEMGET);
					else
					{
						st.soundEffect(SOUND_MIDDLE);
						if(st.ownItemCount(AQUAMARINE) == 10 && st.ownItemCount(CHRYSOBERYL) == 10)
							st.setCond(6);
					}
				}
			}
		}
		else if(npcId == 20480)
			if(cond == 11 && st.ownItemCount(BAT_DIAGRAM) > 0 && st.ownItemCount(STAR_DIAMOND) == 0)
				if(Rnd.chance(50))
				{
					st.takeItems(BAT_DIAGRAM, 1);
					st.giveItems(STAR_DIAMOND, 1);
					st.setCond(12);
					st.soundEffect(SOUND_MIDDLE);
				}
		return null;
	}
}