package quests;

import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.ScriptFile;

public class _283_TheFewTheProudTheBrave extends Quest implements ScriptFile
{
	//NPCs
	private static int PERWAN = 32133;
	//Mobs
	private static int CRIMSON_SPIDER = 22244;
	//Quest Items
	private static int CRIMSON_SPIDER_CLAW = 9747;
	//Chances
	private static int CRIMSON_SPIDER_CLAW_CHANCE = 34;

	public _283_TheFewTheProudTheBrave()
	{
		super(false);
		addStartNpc(PERWAN);
		addKillId(CRIMSON_SPIDER);
		addQuestItem(CRIMSON_SPIDER_CLAW);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int _state = st.getState();
		if(event.equalsIgnoreCase("subelder_perwan_q0283_0103.htm") && _state == CREATED)
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("subelder_perwan_q0283_0203.htm") && _state == STARTED)
		{
			long count = st.ownItemCount(CRIMSON_SPIDER_CLAW);
			if(count > 0)
			{
				st.takeItems(CRIMSON_SPIDER_CLAW, -1);
				st.giveItems(ADENA_ID, 45 * count);

				if(st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4"))
				{
					st.getPlayer().setVar("p1q4", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.DELIVERY_DUTY_COMPLETE, 5000, ScreenMessageAlign.TOP_CENTER, true));
				}

				st.soundEffect(SOUND_MIDDLE);
			}
		}
		else if(event.equalsIgnoreCase("subelder_perwan_q0283_0204.htm") && _state == STARTED)
		{
			st.takeItems(CRIMSON_SPIDER_CLAW, -1);
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != PERWAN)
			return htmltext;
		int _state = st.getState();

		if(_state == CREATED)
		{
			if(st.getPlayer().getLevel() >= 15)
			{
				htmltext = "subelder_perwan_q0283_0101.htm";
				st.setCond(0);
			}
			else
			{
				htmltext = "subelder_perwan_q0283_0102.htm";
				st.exitQuest(true);
			}
		}
		else if(_state == STARTED)
			htmltext = st.ownItemCount(CRIMSON_SPIDER_CLAW) > 0 ? "subelder_perwan_q0283_0105.htm" : "subelder_perwan_q0283_0106.htm";

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.getState() != STARTED)
			return null;

		if(Rnd.chance(CRIMSON_SPIDER_CLAW_CHANCE))
		{
			qs.giveItems(CRIMSON_SPIDER_CLAW, 1);
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