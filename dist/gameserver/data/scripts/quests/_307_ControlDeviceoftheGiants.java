package quests;

import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.RadarControl;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;

/**
 * @author: pchayka
 * @date: 17.02.2011
 */

public class _307_ControlDeviceoftheGiants extends Quest implements ScriptFile
{
	private static final int Droph = 32711;

	private static final int HekatonPrime = 25687;

	private static final int DrophsSupportItems = 14850;
	private static final int CaveExplorationText1Sheet = 14851;
	private static final int CaveExplorationText2Sheet = 14852;
	private static final int CaveExplorationText3Sheet = 14853;

	private static final long HekatonPrimeRespawn = 12 * 3600 * 1000L;

	private static final Location GorgolosLoc = new Location(186096, 61501, -4075, 0);
	private static final Location LastTitanUtenusLoc = new Location(186730, 56456, -4555, 0);
	private static final Location GiantMarpanakLoc = new Location(194057, 53722, -4259, 0);
	private static final Location HekatonPrimeLoc = new Location(192328, 56120, -7651, 0);

	public _307_ControlDeviceoftheGiants()
	{
		super(true);
		addStartNpc(Droph);
		addTalkId(Droph);
		addKillId(HekatonPrime);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("droph_q307_2.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
		}
		else if(event.equalsIgnoreCase("loc1"))
		{
			htmltext = "droph_q307_2a_1.htm";
			RadarControl rc = new RadarControl(0, 1, GorgolosLoc);
			st.getPlayer().sendPacket(rc);
		}
		else if(event.equalsIgnoreCase("loc2"))
		{
			htmltext = "droph_q307_2a_2.htm";
			RadarControl rc = new RadarControl(0, 1, LastTitanUtenusLoc);
			st.getPlayer().sendPacket(rc);
		}
		else if(event.equalsIgnoreCase("loc3"))
		{
			htmltext = "droph_q307_2a_3.htm";
			RadarControl rc = new RadarControl(0, 1, GiantMarpanakLoc);
			st.getPlayer().sendPacket(rc);
		}
		else if(event.equalsIgnoreCase("summon_rb"))
			if(ServerVariables.getLong("HekatonPrimeRespawn", 0) < System.currentTimeMillis() && st.ownItemCount(CaveExplorationText1Sheet) >= 1 && st.ownItemCount(CaveExplorationText2Sheet) >= 1 && st.ownItemCount(CaveExplorationText3Sheet) >= 1)
			{
				st.takeItems(CaveExplorationText1Sheet, 1);
				st.takeItems(CaveExplorationText2Sheet, 1);
				st.takeItems(CaveExplorationText3Sheet, 1);
				ServerVariables.set("HekatonPrimeRespawn", System.currentTimeMillis() + HekatonPrimeRespawn);
				NpcInstance boss = st.addSpawn(HekatonPrime, HekatonPrimeLoc.x, HekatonPrimeLoc.y, HekatonPrimeLoc.z, HekatonPrimeLoc.h, 0, 0);
				boss.getMinionList().spawnMinions();
				htmltext = "droph_q307_3a.htm";
			}
			else
				htmltext = "droph_q307_2b.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Droph)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 79)
					htmltext = "droph_q307_1.htm";
				else
				{
					htmltext = "droph_q307_0.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
			{
				if(st.ownItemCount(CaveExplorationText1Sheet) >= 1 && st.ownItemCount(CaveExplorationText2Sheet) >= 1 && st.ownItemCount(CaveExplorationText3Sheet) >= 1)
					if(ServerVariables.getLong("HekatonPrimeRespawn", 0) < System.currentTimeMillis())
						htmltext = "droph_q307_3.htm";
					else
						htmltext = "droph_q307_4.htm";
				else
					htmltext = "droph_q307_2a.htm";
			}
			else if(cond == 2)
			{
				htmltext = "droph_q307_5.htm";
				st.giveItems(DrophsSupportItems, 1);
				st.soundEffect(SOUND_FINISH);
				st.exitQuest(true);
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == HekatonPrime)
			st.setCond(2);
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