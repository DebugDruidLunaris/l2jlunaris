package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _313_CollectSpores extends Quest implements ScriptFile
{
	//NPC
	public final int Herbiel = 30150;
	//Mobs
	public final int SporeFungus = 20509;
	//Quest Items
	public final int SporeSac = 1118;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _313_CollectSpores()
	{
		super(false);

		addStartNpc(Herbiel);
		addTalkId(Herbiel);
		addKillId(SporeFungus);
		addQuestItem(SporeSac);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("green_q0313_05.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8)
				htmltext = "green_q0313_03.htm";
			else
			{
				htmltext = "green_q0313_02.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			htmltext = "green_q0313_06.htm";
		else if(cond == 2)
			if(st.ownItemCount(SporeSac) < 10)
			{
				st.setCond(1);
				htmltext = "green_q0313_06.htm";
			}
			else
			{
				st.takeItems(SporeSac, -1);
				st.giveItems(ADENA_ID, 3500, true);
				st.soundEffect(SOUND_FINISH);
				htmltext = "green_q0313_07.htm";
				st.exitQuest(true);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == SporeFungus && Rnd.chance(70))
		{
			st.giveItems(SporeSac, 1);
			if(st.ownItemCount(SporeSac) < 10)
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