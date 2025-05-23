package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

public class _316_DestroyPlaguebringers extends Quest implements ScriptFile
{
	//NPCs
	private static int Ellenia = 30155;
	//Mobs
	private static int Sukar_Wererat = 20040;
	private static int Sukar_Wererat_Leader = 20047;
	private static int Varool_Foulclaw = 27020;
	//Quest Items
	private static int Wererats_Fang = 1042;
	private static int Varool_Foulclaws_Fang = 1043;
	//Chances
	private static int Wererats_Fang_Chance = 50;
	private static int Varool_Foulclaws_Fang_Chance = 30;

	public _316_DestroyPlaguebringers()
	{
		super(false);
		addStartNpc(Ellenia);
		addKillId(Sukar_Wererat);
		addKillId(Sukar_Wererat_Leader);
		addKillId(Varool_Foulclaw);
		addQuestItem(Wererats_Fang);
		addQuestItem(Varool_Foulclaws_Fang);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("elliasin_q0316_04.htm") && _state == CREATED && st.getPlayer().getRace() == Race.elf && st.getPlayer().getLevel() >= 18)
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("elliasin_q0316_08.htm") && _state == STARTED)
		{
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != Ellenia)
			return htmltext;
		int _state = st.getState();

		if(_state == CREATED)
		{
			if(st.getPlayer().getRace() != Race.elf)
			{
				htmltext = "elliasin_q0316_00.htm";
				st.exitQuest(true);
			}
			else if(st.getPlayer().getLevel() < 18)
			{
				htmltext = "elliasin_q0316_02.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "elliasin_q0316_03.htm";
				st.setCond(0);
			}
		}
		else if(_state == STARTED)
		{
			long Reward = st.ownItemCount(Wererats_Fang) * 60 + st.ownItemCount(Varool_Foulclaws_Fang) * 10000L;
			if(Reward > 0)
			{
				htmltext = "elliasin_q0316_07.htm";
				st.takeItems(Wererats_Fang, -1);
				st.takeItems(Varool_Foulclaws_Fang, -1);
				st.giveItems(ADENA_ID, Reward);
				st.soundEffect(SOUND_MIDDLE);
			}
			else
				htmltext = "elliasin_q0316_05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != STARTED)
			return null;

		if(npc.getNpcId() == Varool_Foulclaw && qs.ownItemCount(Varool_Foulclaws_Fang) == 0 && Rnd.chance(Varool_Foulclaws_Fang_Chance))
		{
			qs.giveItems(Varool_Foulclaws_Fang, 1);
			qs.soundEffect(SOUND_ITEMGET);
		}
		else if(Rnd.chance(Wererats_Fang_Chance))
		{
			qs.giveItems(Wererats_Fang, 1);
			qs.soundEffect(SOUND_ITEMGET);
		}

		return null;
	}

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}