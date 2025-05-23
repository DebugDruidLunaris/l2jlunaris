package quests;

import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _268_TracesOfEvil extends Quest implements ScriptFile
{
	//NPC
	public final int KUNAI = 30559;
	//MOBS
	public final int SPIDER = 20474;
	public final int FANG_SPIDER = 20476;
	public final int BLADE_SPIDER = 20478;
	//ITEMS
	public final int CONTAMINATED = 10869;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _268_TracesOfEvil()
	{
		super(false);
		addStartNpc(KUNAI);
		addKillId(SPIDER, FANG_SPIDER, BLADE_SPIDER);
		addQuestItem(CONTAMINATED);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("trader_kunai_q0268_03.htm"))
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
		if(st.getCond() == 0)
			if(st.getPlayer().getLevel() < 15)
			{
				htmltext = "trader_kunai_q0268_02.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "trader_kunai_q0268_01.htm";
		else if(st.ownItemCount(CONTAMINATED) >= 30)
		{
			htmltext = "trader_kunai_q0268_06.htm";
			st.giveItems(ADENA_ID, 2474);
			st.addExpAndSp(8738, 409);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		else
			htmltext = "trader_kunai_q0268_04.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		st.giveItems(CONTAMINATED, 1);
		if(st.ownItemCount(CONTAMINATED) <= 29)
			st.soundEffect(SOUND_ITEMGET);
		else if(st.ownItemCount(CONTAMINATED) >= 30)
		{
			st.soundEffect(SOUND_MIDDLE);
			st.setCond(2);
			st.setState(STARTED);
		}
		return null;
	}
}