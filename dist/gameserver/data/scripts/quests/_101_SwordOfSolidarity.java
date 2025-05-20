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

public class _101_SwordOfSolidarity extends Quest implements ScriptFile
{
	int ROIENS_LETTER = 796;
	int HOWTOGO_RUINS = 937;
	int BROKEN_SWORD_HANDLE = 739;
	int BROKEN_BLADE_BOTTOM = 740;
	int BROKEN_BLADE_TOP = 741;
	int ALLTRANS_NOTE = 742;
	int SWORD_OF_SOLIDARITY = 738;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _101_SwordOfSolidarity()
	{
		super(false);

		addStartNpc(30008);
		addTalkId(30283);

		addKillId(20361);
		addKillId(20362);

		addQuestItem(ALLTRANS_NOTE, HOWTOGO_RUINS, BROKEN_BLADE_TOP, BROKEN_BLADE_BOTTOM, ROIENS_LETTER, BROKEN_SWORD_HANDLE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("roien_q0101_04.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			st.giveItems(ROIENS_LETTER, 1);
		}
		else if(event.equalsIgnoreCase("blacksmith_alltran_q0101_02.htm"))
		{
			st.setCond(2);
			st.takeItems(ROIENS_LETTER, -1);
			st.giveItems(HOWTOGO_RUINS, 1);
		}
		else if(event.equalsIgnoreCase("blacksmith_alltran_q0101_07.htm"))
		{
			st.takeItems(BROKEN_SWORD_HANDLE, -1);

			st.giveItems(SWORD_OF_SOLIDARITY, 1);
			st.giveItems(ADENA_ID, 10981, false);
			st.getPlayer().addExpAndSp(25747, 2171);

			if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3"))
			{
				st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
				st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
				st.giveItems(1060, 100); // healing potion
				for(int item = 4412; item <= 4417; item++)
					st.giveItems(item, 10); // echo cry
				if(st.getPlayer().getClassId().isMage())
				{
					st.playTutorialVoice("tutorial_voice_027");
					st.giveItems(5790, 3000); // newbie sps
				}
				else
				{
					st.playTutorialVoice("tutorial_voice_026");
					st.giveItems(5789, 6000); // newbie ss
				}
			}

			st.exitQuest(true);
			st.soundEffect(SOUND_FINISH);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30008)
		{
			if(cond == 0)
			{

				if(st.getPlayer().getRace() != Race.human)
					htmltext = "roien_q0101_00.htm";
				else if(st.getPlayer().getLevel() >= 9)
				{
					htmltext = "roien_q0101_02.htm";
					return htmltext;
				}
				else
				{
					htmltext = "roien_q0101_08.htm";
					st.exitQuest(true);
				}

			}
			else if(cond == 1 && st.ownItemCount(ROIENS_LETTER) == 1)
				htmltext = "roien_q0101_05.htm";
			else if(cond >= 2 && st.ownItemCount(ROIENS_LETTER) == 0 && st.ownItemCount(ALLTRANS_NOTE) == 0)
			{
				if(st.ownItemCount(BROKEN_BLADE_TOP) > 0 && st.ownItemCount(BROKEN_BLADE_BOTTOM) > 0)
					htmltext = "roien_q0101_12.htm";
				if(st.ownItemCount(BROKEN_BLADE_TOP) + st.ownItemCount(BROKEN_BLADE_BOTTOM) <= 1)
					htmltext = "roien_q0101_11.htm";
				if(st.ownItemCount(BROKEN_SWORD_HANDLE) > 0)
					htmltext = "roien_q0101_07.htm";
				if(st.ownItemCount(HOWTOGO_RUINS) == 1)
					htmltext = "roien_q0101_10.htm";
			}
			else if(cond == 4 && st.ownItemCount(ALLTRANS_NOTE) > 0)
			{
				htmltext = "roien_q0101_06.htm";
				st.setCond(5);
				st.takeItems(ALLTRANS_NOTE, -1);
				st.giveItems(BROKEN_SWORD_HANDLE, 1);
			}
		}
		else if(npcId == 30283)
			if(cond == 1 && st.ownItemCount(ROIENS_LETTER) > 0)
				htmltext = "blacksmith_alltran_q0101_01.htm";
			else if(cond >= 2 && st.ownItemCount(HOWTOGO_RUINS) == 1)
			{
				if(st.ownItemCount(BROKEN_BLADE_TOP) + st.ownItemCount(BROKEN_BLADE_BOTTOM) == 1)
					htmltext = "blacksmith_alltran_q0101_08.htm";
				else if(st.ownItemCount(BROKEN_BLADE_TOP) + st.ownItemCount(BROKEN_BLADE_BOTTOM) == 0)
					htmltext = "blacksmith_alltran_q0101_03.htm";
				else if(st.ownItemCount(BROKEN_BLADE_TOP) > 0 && st.ownItemCount(BROKEN_BLADE_BOTTOM) > 0)
				{
					htmltext = "blacksmith_alltran_q0101_04.htm";
					st.setCond(4);
					st.takeItems(HOWTOGO_RUINS, -1);
					st.takeItems(BROKEN_BLADE_TOP, -1);
					st.takeItems(BROKEN_BLADE_BOTTOM, -1);
					st.giveItems(ALLTRANS_NOTE, 1);
				}
				else if(cond == 4 && st.ownItemCount(ALLTRANS_NOTE) > 0)
					htmltext = "blacksmith_alltran_q0101_05.htm";
			}
			else if(cond == 5 && st.ownItemCount(BROKEN_SWORD_HANDLE) > 0)
				htmltext = "blacksmith_alltran_q0101_06.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if((npcId == 20361 || npcId == 20362) && st.ownItemCount(HOWTOGO_RUINS) > 0)
		{
			if(st.ownItemCount(BROKEN_BLADE_TOP) == 0 && Rnd.chance(60))
			{
				st.giveItems(BROKEN_BLADE_TOP, 1);
				st.soundEffect(SOUND_MIDDLE);
			}
			else if(st.ownItemCount(BROKEN_BLADE_BOTTOM) == 0 && Rnd.chance(60))
			{
				st.giveItems(BROKEN_BLADE_BOTTOM, 1);
				st.soundEffect(SOUND_MIDDLE);
			}
			if(st.ownItemCount(BROKEN_BLADE_TOP) > 0 && st.ownItemCount(BROKEN_BLADE_BOTTOM) > 0)
				st.setCond(3);
		}
		return null;
	}
}