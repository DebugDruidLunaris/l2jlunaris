package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _291_RevengeOfTheRedbonnet extends Quest implements ScriptFile
{
	//NPC
	int MaryseRedbonnet = 30553;
	//Quest Items
	int BlackWolfPelt = 1482;
	//Item
	int ScrollOfEscape = 736;
	int GrandmasPearl = 1502;
	int GrandmasMirror = 1503;
	int GrandmasNecklace = 1504;
	int GrandmasHairpin = 1505;
	//Mobs
	int BlackWolf = 20317;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _291_RevengeOfTheRedbonnet()
	{
		super(false);

		addStartNpc(MaryseRedbonnet);
		addTalkId(MaryseRedbonnet);

		addKillId(BlackWolf);

		addQuestItem(BlackWolfPelt);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("marife_redbonnet_q0291_03.htm"))
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
			if(st.getPlayer().getLevel() < 4)
			{
				htmltext = "marife_redbonnet_q0291_01.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "marife_redbonnet_q0291_02.htm";
		}
		else if(cond == 1)
			htmltext = "marife_redbonnet_q0291_04.htm";
		else if(cond == 2 && st.ownItemCount(BlackWolfPelt) < 40)
		{
			htmltext = "marife_redbonnet_q0291_04.htm";
			st.setCond(1);
		}
		else if(cond == 2 && st.ownItemCount(BlackWolfPelt) >= 40)
		{
			int random = Rnd.get(100);
			st.takeItems(BlackWolfPelt, -1);
			if(random < 3)
				st.giveItems(GrandmasPearl, 1);
			else if(random < 21)
				st.giveItems(GrandmasMirror, 1);
			else if(random < 46)
				st.giveItems(GrandmasNecklace, 1);
			else
			{
				st.giveItems(ScrollOfEscape, 1);
				st.giveItems(GrandmasHairpin, 1);
			}
			htmltext = "marife_redbonnet_q0291_05.htm";
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.ownItemCount(BlackWolfPelt) < 40)
		{
			st.giveItems(BlackWolfPelt, 1);
			if(st.ownItemCount(BlackWolfPelt) < 40)
				st.soundEffect(SOUND_ITEMGET);
			else
			{
				st.soundEffect(SOUND_MIDDLE);
				st.setCond(2);
				st.setState(STARTED);
			}
		}
		return null;
	}
}
