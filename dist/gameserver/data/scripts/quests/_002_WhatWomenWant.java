package quests;

import jts.gameserver.Config;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

/**
 * Рейты учтены
 */
public class _002_WhatWomenWant extends Quest implements ScriptFile
{
	int ARUJIEN = 30223;
	int MIRABEL = 30146;
	int HERBIEL = 30150;
	int GREENIS = 30157;

	int ARUJIENS_LETTER1 = 1092;
	int ARUJIENS_LETTER2 = 1093;
	int ARUJIENS_LETTER3 = 1094;
	int POETRY_BOOK = 689;
	int GREENIS_LETTER = 693;

	int MYSTICS_EARRING = 113;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _002_WhatWomenWant()
	{
		super(false);
		addStartNpc(ARUJIEN);

		addTalkId(MIRABEL);
		addTalkId(HERBIEL);
		addTalkId(GREENIS);

		addQuestItem(GREENIS_LETTER, ARUJIENS_LETTER3, ARUJIENS_LETTER1, ARUJIENS_LETTER2, POETRY_BOOK);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "arujien_q0002_04.htm";
			st.giveItems(ARUJIENS_LETTER1, 1, false);
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("2_1"))
		{
			htmltext = "arujien_q0002_08.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(POETRY_BOOK, 1, false);
			st.setCond(4);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("2_2"))
		{
			htmltext = "arujien_q0002_09.htm";
			st.takeItems(ARUJIENS_LETTER3, -1);
			st.giveItems(ADENA_ID, 2300, true);
			st.getPlayer().addExpAndSp(4254, 335);
			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == ARUJIEN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.elf && st.getPlayer().getRace() != Race.human)
					htmltext = "arujien_q0002_00.htm";
				else if(st.getPlayer().getLevel() >= 2)
					htmltext = "arujien_q0002_02.htm";
				else
				{
					htmltext = "arujien_q0002_01.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 && st.ownItemCount(ARUJIENS_LETTER1) > 0)
				htmltext = "arujien_q0002_05.htm";
			else if(cond == 2 && st.ownItemCount(ARUJIENS_LETTER2) > 0)
				htmltext = "arujien_q0002_06.htm";
			else if(cond == 3 && st.ownItemCount(ARUJIENS_LETTER3) > 0)
				htmltext = "arujien_q0002_07.htm";
			else if(cond == 4 && st.ownItemCount(POETRY_BOOK) > 0)
				htmltext = "arujien_q0002_11.htm";
			else if(cond == 5 && st.ownItemCount(GREENIS_LETTER) > 0)
			{
				htmltext = "arujien_q0002_09.htm";
				st.takeItems(GREENIS_LETTER, -1);
				st.giveItems(MYSTICS_EARRING, 1, false);
				st.giveItems(ADENA_ID, (int) ((Config.RATE_QUESTS_REWARD - 1) * 620 + 1850 * Config.RATE_QUESTS_REWARD), false); // T2
				st.getPlayer().addExpAndSp(4254, 335);
				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1"))
					st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if(npcId == MIRABEL)
		{
			if(cond == 1 && st.ownItemCount(ARUJIENS_LETTER1) > 0)
			{
				htmltext = "mint_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER1, -1);
				st.giveItems(ARUJIENS_LETTER2, 1, false);
				st.setCond(2);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 2)
				htmltext = "mint_q0002_02.htm";
		}
		else if(npcId == HERBIEL)
		{
			if(cond == 2 && st.ownItemCount(ARUJIENS_LETTER2) > 0)
			{
				htmltext = "green_q0002_01.htm";
				st.takeItems(ARUJIENS_LETTER2, -1);
				st.giveItems(ARUJIENS_LETTER3, 1, false);
				st.setCond(3);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 3)
				htmltext = "green_q0002_02.htm";
		}
		else if(npcId == GREENIS)
			if(cond == 4 && st.ownItemCount(POETRY_BOOK) > 0)
			{
				htmltext = "grain_q0002_02.htm";
				st.takeItems(POETRY_BOOK, -1);
				st.giveItems(GREENIS_LETTER, 1, false);
				st.setCond(5);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(cond == 5 && st.ownItemCount(GREENIS_LETTER) > 0)
				htmltext = "grain_q0002_03.htm";
			else
				htmltext = "grain_q0002_01.htm";
		return htmltext;
	}
}
