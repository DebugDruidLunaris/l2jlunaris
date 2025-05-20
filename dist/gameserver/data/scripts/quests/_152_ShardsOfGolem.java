package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _152_ShardsOfGolem extends Quest implements ScriptFile
{
	int HARRYS_RECEIPT1 = 1008;
	int HARRYS_RECEIPT2 = 1009;
	int GOLEM_SHARD = 1010;
	int TOOL_BOX = 1011;
	int WOODEN_BP = 23;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _152_ShardsOfGolem()
	{
		super(false);

		addStartNpc(30035);

		addTalkId(30035);
		addTalkId(30035);
		addTalkId(30283);
		addTalkId(30035);

		addKillId(20016);
		addKillId(20101);

		addQuestItem(new int[] { HARRYS_RECEIPT1, GOLEM_SHARD, TOOL_BOX, HARRYS_RECEIPT2 });
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("30035-04.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			if(st.ownItemCount(HARRYS_RECEIPT1) == 0)
				st.giveItems(HARRYS_RECEIPT1, 1);
		}
		else if(event.equals("152_2"))
		{
			st.takeItems(HARRYS_RECEIPT1, -1);
			if(st.ownItemCount(HARRYS_RECEIPT2) == 0)
			{
				st.giveItems(HARRYS_RECEIPT2, 1);
				st.setCond(2);
			}
			htmltext = "30283-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		int cond = st.getCond();
		if(npcId == 30035)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 10)
				{
					htmltext = "30035-03.htm";
					return htmltext;
				}
				htmltext = "30035-02.htm";
				st.exitQuest(true);
			}
			else if(cond == 1 && st.ownItemCount(HARRYS_RECEIPT1) != 0)
				htmltext = "30035-05.htm";
			else if(cond == 2 && st.ownItemCount(HARRYS_RECEIPT2) != 0)
				htmltext = "30035-05.htm";
			else if(cond == 4 && st.ownItemCount(TOOL_BOX) != 0)
			{
				st.takeItems(TOOL_BOX, -1);
				st.takeItems(HARRYS_RECEIPT2, -1);
				st.setCond(0);
				st.soundEffect(SOUND_FINISH);
				st.giveItems(WOODEN_BP, 1);
				st.addExpAndSp(5000, 0);
				htmltext = "30035-06.htm";
				st.exitQuest(false);
			}
		}
		else if(npcId == 30283)
		{
			if(cond == 1 && st.ownItemCount(HARRYS_RECEIPT1) != 0)
				htmltext = "30283-01.htm";
			else if(cond == 2 && st.ownItemCount(HARRYS_RECEIPT2) != 0 && st.ownItemCount(GOLEM_SHARD) < 5)
				htmltext = "30283-03.htm";
			else if(cond == 3 && st.ownItemCount(HARRYS_RECEIPT2) != 0 && st.ownItemCount(GOLEM_SHARD) == 5)
			{
				st.takeItems(GOLEM_SHARD, -1);
				if(st.ownItemCount(TOOL_BOX) == 0)
				{
					st.giveItems(TOOL_BOX, 1);
					st.setCond(4);
				}
				htmltext = "30283-04.htm";
			}
		}
		else if(cond == 4 && st.ownItemCount(HARRYS_RECEIPT2) != 0 && st.ownItemCount(TOOL_BOX) != 0)
			htmltext = "30283-05.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 2 && Rnd.chance(30) && st.ownItemCount(GOLEM_SHARD) < 5)
		{
			st.giveItems(GOLEM_SHARD, 1);
			if(st.ownItemCount(GOLEM_SHARD) == 5)
			{
				st.setCond(3);
				st.soundEffect(SOUND_MIDDLE);
			}
			else
				st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}