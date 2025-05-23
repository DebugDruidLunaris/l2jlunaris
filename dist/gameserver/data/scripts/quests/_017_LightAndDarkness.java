package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _017_LightAndDarkness extends Quest implements ScriptFile
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

	public _017_LightAndDarkness()
	{
		super(false);

		addStartNpc(31517);

		addTalkId(31508);
		addTalkId(31509);
		addTalkId(31510);
		addTalkId(31511);

		addQuestItem(7168);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("dark_presbyter_q0017_04.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.giveItems(7168, 4);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("blessed_altar1_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equals("blessed_altar2_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(3);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equals("blessed_altar3_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(4);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equals("blessed_altar4_q0017_02.htm"))
		{
			st.takeItems(7168, 1);
			st.setCond(5);
			st.soundEffect(SOUND_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 31517)
		{
			if(cond == 0)
				if(st.getPlayer().getLevel() >= 61)
					htmltext = "dark_presbyter_q0017_01.htm";
				else
				{
					htmltext = "dark_presbyter_q0017_03.htm";
					st.exitQuest(true);
				}
			else if(cond > 0 && cond < 5 && st.ownItemCount(7168) > 0)
				htmltext = "dark_presbyter_q0017_05.htm";
			else if(cond > 0 && cond < 5 && st.ownItemCount(7168) == 0)
			{
				htmltext = "dark_presbyter_q0017_06.htm";
				st.setCond(0);
				st.exitQuest(false);
			}
			else if(cond == 5 && st.ownItemCount(7168) == 0)
			{
				htmltext = "dark_presbyter_q0017_07.htm";
				st.addExpAndSp(697040, 54887);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if(npcId == 31508)
		{
			if(cond == 1)
				if(st.ownItemCount(7168) != 0)
					htmltext = "blessed_altar1_q0017_01.htm";
				else
					htmltext = "blessed_altar1_q0017_03.htm";
			else if(cond == 2)
				htmltext = "blessed_altar1_q0017_05.htm";
		}
		else if(npcId == 31509)
		{
			if(cond == 2)
				if(st.ownItemCount(7168) != 0)
					htmltext = "blessed_altar2_q0017_01.htm";
				else
					htmltext = "blessed_altar2_q0017_03.htm";
			else if(cond == 3)
				htmltext = "blessed_altar2_q0017_05.htm";
		}
		else if(npcId == 31510)
		{
			if(cond == 3)
				if(st.ownItemCount(7168) != 0)
					htmltext = "blessed_altar3_q0017_01.htm";
				else
					htmltext = "blessed_altar3_q0017_03.htm";
			else if(cond == 4)
				htmltext = "blessed_altar3_q0017_05.htm";
		}
		else if(npcId == 31511)
			if(cond == 4)
				if(st.ownItemCount(7168) != 0)
					htmltext = "blessed_altar4_q0017_01.htm";
				else
					htmltext = "blessed_altar4_q0017_03.htm";
			else if(cond == 5)
				htmltext = "blessed_altar4_q0017_05.htm";
		return htmltext;
	}
}