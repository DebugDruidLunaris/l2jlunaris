package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _258_BringWolfPelts extends Quest implements ScriptFile
{
	int WOLF_PELT = 702;

	int Cotton_Shirt = 390;
	int Leather_Pants = 29;
	int Leather_Shirt = 22;
	int Short_Leather_Gloves = 1119;
	int Tunic = 426;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _258_BringWolfPelts()
	{
		super(false);

		addStartNpc(30001);
		addKillId(20120);
		addKillId(20442);

		addQuestItem(WOLF_PELT);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.intern().equalsIgnoreCase("lector_q0258_03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 3)
			{
				htmltext = "lector_q0258_02.htm";
				return htmltext;
			}
			htmltext = "lector_q0258_01.htm";
			st.exitQuest(true);
		}
		else if(cond == 1 && st.ownItemCount(WOLF_PELT) >= 0 && st.ownItemCount(WOLF_PELT) < 40)
			htmltext = "lector_q0258_05.htm";
		else if(cond == 2 && st.ownItemCount(WOLF_PELT) >= 40)
		{
			st.takeItems(WOLF_PELT, 40);
			int n = Rnd.get(16);
			if(n == 0)
			{
				st.giveItems(Cotton_Shirt, 1);
				st.soundEffect(SOUND_JACKPOT);
			}
			else if(n < 6)
				st.giveItems(Leather_Pants, 1);
			else if(n < 9)
				st.giveItems(Leather_Shirt, 1);
			else if(n < 13)
				st.giveItems(Short_Leather_Gloves, 1);
			else
				st.giveItems(Tunic, 1);
			htmltext = "lector_q0258_06.htm";
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		long count = st.ownItemCount(WOLF_PELT);
		if(count < 40 && st.getCond() == 1)
		{
			st.giveItems(WOLF_PELT, 1);
			if(count == 39)
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