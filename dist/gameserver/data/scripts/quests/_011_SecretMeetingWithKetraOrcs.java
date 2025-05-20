package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _011_SecretMeetingWithKetraOrcs extends Quest implements ScriptFile
{
	int CADMON = 31296;
	int LEON = 31256;
	int WAHKAN = 31371;

	int MUNITIONS_BOX = 7231;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _011_SecretMeetingWithKetraOrcs()
	{
		super(false);

		addStartNpc(CADMON);

		addTalkId(LEON);
		addTalkId(WAHKAN);

		addQuestItem(MUNITIONS_BOX);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("guard_cadmon_q0011_0104.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("trader_leon_q0011_0201.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1);
			st.setCond(2);
			st.soundEffect(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("herald_wakan_q0011_0301.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(82045, 6047);
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
		if(npcId == CADMON)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 74)
					htmltext = "guard_cadmon_q0011_0101.htm";
				else
				{
					htmltext = "guard_cadmon_q0011_0103.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "guard_cadmon_q0011_0105.htm";
		}
		else if(npcId == LEON)
		{
			if(cond == 1)
				htmltext = "trader_leon_q0011_0101.htm";
			else if(cond == 2)
				htmltext = "trader_leon_q0011_0202.htm";
		}
		else if(npcId == WAHKAN)
			if(cond == 2 && st.ownItemCount(MUNITIONS_BOX) > 0)
				htmltext = "herald_wakan_q0011_0201.htm";
		return htmltext;
	}
}
