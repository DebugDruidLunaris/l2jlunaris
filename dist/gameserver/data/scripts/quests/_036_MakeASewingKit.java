package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _036_MakeASewingKit extends Quest implements ScriptFile
{
	int REINFORCED_STEEL = 7163;
	int ARTISANS_FRAME = 1891;
	int ORIHARUKON = 1893;
	int SEWING_KIT = 7078;

	@Override
	public void onLoad()
	{
	}

	@Override
	public void onReload()
	{
	}

	@Override
	public void onShutdown()
	{
	}

	public _036_MakeASewingKit()
	{
		super(false);

		addStartNpc(30847);
		addTalkId(30847);
		addTalkId(30847);

		addKillId(20566);

		addQuestItem(REINFORCED_STEEL);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		int cond = st.getCond();
		if (event.equals("head_blacksmith_ferris_q0036_0104.htm") && cond == 0)
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if (event.equals("head_blacksmith_ferris_q0036_0201.htm") && cond == 2)
		{
			st.takeItems(REINFORCED_STEEL, 5);
			st.setCond(3);
		}
		else if (event.equals("head_blacksmith_ferris_q0036_0301.htm"))
			if (st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10)
			{
				st.takeItems(ORIHARUKON, 10);
				st.takeItems(ARTISANS_FRAME, 10);
				st.giveItems(SEWING_KIT, 1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "head_blacksmith_ferris_q0036_0203.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if (cond == 0 && st.getQuestItemsCount(SEWING_KIT) == 0)
		{
			if (st.getPlayer().getLevel() >= 60)
			{
				QuestState fwear = st.getPlayer().getQuestState(_037_MakeFormalWear.class);
				if (fwear != null && fwear.getState() == STARTED)
				{
					if (fwear.getCond() == 6)
						htmltext = "head_blacksmith_ferris_q0036_0101.htm";
					else
						st.exitQuest(true);
				}
				else
					st.exitQuest(true);
			}
			else
				htmltext = "head_blacksmith_ferris_q0036_0103.htm";
		}
		else if (cond == 1 && st.getQuestItemsCount(REINFORCED_STEEL) < 5)
			htmltext = "head_blacksmith_ferris_q0036_0106.htm";
		else if (cond == 2 && st.getQuestItemsCount(REINFORCED_STEEL) == 5)
			htmltext = "head_blacksmith_ferris_q0036_0105.htm";
		else if (cond == 3 && (st.getQuestItemsCount(ORIHARUKON) < 10 || st.getQuestItemsCount(ARTISANS_FRAME) < 10))
			htmltext = "head_blacksmith_ferris_q0036_0204.htm";
		else if (cond == 3 && st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10)
			htmltext = "head_blacksmith_ferris_q0036_0203.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if (st.getQuestItemsCount(REINFORCED_STEEL) < 5)
		{
			st.giveItems(REINFORCED_STEEL, 1);
			if (st.getQuestItemsCount(REINFORCED_STEEL) == 5)
			{
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(2);
			}
			else
				st.soundEffect(SOUND_ITEMGET);
		}
		return null;
	}
}