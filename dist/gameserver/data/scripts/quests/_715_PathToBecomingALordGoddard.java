package quests;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author pchayka
 */

public class _715_PathToBecomingALordGoddard extends Quest implements ScriptFile
{
	private static final int Alfred = 35363;

	private static final int WaterSpiritAshutar = 25316;
	private static final int FireSpiritNastron = 25306;

	private static final int GoddardCastle = 7;

	public _715_PathToBecomingALordGoddard()
	{
		super(false);
		addStartNpc(Alfred);
		addKillId(WaterSpiritAshutar, FireSpiritNastron);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		Castle castle = ResidenceHolder.getInstance().getResidence(GoddardCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(event.equals("alfred_q715_03.htm"))
		{
			st.setState(STARTED);
			st.setCond(1);
			st.soundEffect(SOUND_ACCEPT);
		}
		else if(event.equals("alfred_q715_04a.htm"))
			st.setCond(3);
		else if(event.equals("alfred_q715_04b.htm"))
			st.setCond(2);
		else if(event.equals("alfred_q715_08.htm"))
		{
			castle.getDominion().changeOwner(castleOwner.getClan());
			st.soundEffect(SOUND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	@SuppressWarnings("unused")
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		Castle castle = ResidenceHolder.getInstance().getResidence(GoddardCastle);
		if(castle.getOwner() == null)
			return "Castle has no lord";
		Player castleOwner = castle.getOwner().getLeader().getPlayer();

		if(cond == 0)
		{
			if(castleOwner == st.getPlayer())
			{
				if(castle.getDominion().getLordObjectId() != st.getPlayer().getObjectId())
					htmltext = "alfred_q715_01.htm";
				else
				{
					htmltext = "alfred_q715_00.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "alfred_q715_00a.htm";
				st.exitQuest(true);
			}
		}
		else if(cond == 1)
			htmltext = "alfred_q715_03.htm";
		else if(cond == 2)
			htmltext = "alfred_q715_05b.htm";
		else if(cond == 3)
			htmltext = "alfred_q715_05a.htm";
		else if(cond == 4)
		{
			st.setCond(6);
			htmltext = "alfred_q715_06b.htm";
		}
		else if(cond == 5)
		{
			st.setCond(7);
			htmltext = "alfred_q715_06a.htm";
		}
		else if(cond == 6)
			htmltext = "alfred_q715_06b.htm";
		else if(cond == 7)
			htmltext = "alfred_q715_06a.htm";
		else if(cond == 8 || cond == 9)
			htmltext = "alfred_q715_07.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 2 && npc.getNpcId() == FireSpiritNastron)
			st.setCond(4);
		else if(st.getCond() == 3 && npc.getNpcId() == WaterSpiritAshutar)
			st.setCond(5);

		if(st.getCond() == 6 && npc.getNpcId() == WaterSpiritAshutar)
			st.setCond(9);
		else if(st.getCond() == 7 && npc.getNpcId() == FireSpiritNastron)
			st.setCond(8);
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