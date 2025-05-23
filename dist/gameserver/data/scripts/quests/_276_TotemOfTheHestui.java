package quests;

import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

public class _276_TotemOfTheHestui extends Quest implements ScriptFile
{
	//NPCs
	private static int Tanapi = 30571;
	//Mobs
	private static int Kasha_Bear = 20479;
	private static int Kasha_Bear_Totem_Spirit = 27044;
	//Items
	private static int Leather_Pants = 29;
	private static int Totem_of_Hestui = 1500;
	//Quest Items
	private static int Kasha_Parasite = 1480;
	private static int Kasha_Crystal = 1481;

	public _276_TotemOfTheHestui()
	{
		super(false);
		addStartNpc(Tanapi);
		addKillId(Kasha_Bear);
		addKillId(Kasha_Bear_Totem_Spirit);
		addQuestItem(Kasha_Parasite);
		addQuestItem(Kasha_Crystal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("seer_tanapi_q0276_03.htm") && st.getState() == CREATED && st.getPlayer().getRace() == Race.orc && st.getPlayer().getLevel() >= 15)
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != Tanapi)
			return htmltext;
		int _state = st.getState();

		if(_state == CREATED)
		{
			if(st.getPlayer().getRace() != Race.orc)
			{
				htmltext = "seer_tanapi_q0276_00.htm";
				st.exitQuest(true);
			}
			else if(st.getPlayer().getLevel() < 15)
			{
				htmltext = "seer_tanapi_q0276_01.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "seer_tanapi_q0276_02.htm";
				st.setCond(0);
			}
		}
		else if(_state == STARTED)
			if(st.ownItemCount(Kasha_Crystal) > 0)
			{
				htmltext = "seer_tanapi_q0276_05.htm";
				st.takeItems(Kasha_Parasite, -1);
				st.takeItems(Kasha_Crystal, -1);

				st.giveItems(Leather_Pants, 1);
				st.giveItems(Totem_of_Hestui, 1);
				if(st.getRateQuestsReward() > 1)
					st.giveItems(57, Math.round(ItemHolder.getInstance().getTemplate(Totem_of_Hestui).getReferencePrice() * (st.getRateQuestsReward() - 1) / 2), false);

				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
				{
					st.getPlayer().setVar("p1q4", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
				}

				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "seer_tanapi_q0276_04.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != STARTED)
			return null;
		int npcId = npc.getNpcId();

		if(npcId == Kasha_Bear && qs.ownItemCount(Kasha_Crystal) == 0)
		{
			if(qs.ownItemCount(Kasha_Parasite) < 50)
			{
				qs.giveItems(Kasha_Parasite, 1);
				qs.soundEffect(SOUND_ITEMGET);
			}
			else
			{
				qs.takeItems(Kasha_Parasite, -1);
				qs.addSpawn(Kasha_Bear_Totem_Spirit);
			}
		}
		else if(npcId == Kasha_Bear_Totem_Spirit && qs.ownItemCount(Kasha_Crystal) == 0)
		{
			qs.giveItems(Kasha_Crystal, 1);
			qs.soundEffect(SOUND_MIDDLE);
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