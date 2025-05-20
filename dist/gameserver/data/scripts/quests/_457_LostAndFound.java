package quests;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;

public class _457_LostAndFound extends Quest implements ScriptFile
{
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int Gumiel = 32738;

	private ScheduledFuture<?> FollowTask;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public _457_LostAndFound()
	{
		super(true);
		addStartNpc(32759);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		Player player = st.getPlayer();
		@SuppressWarnings("unused")
		String htmltext = event;
		if(event.equalsIgnoreCase("lost_villager_q0457_06.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.soundEffect(SOUND_ACCEPT);
			DefaultAI.namechar = player.getName();
			if(seeSoldier(npc, st.getPlayer()) == null)
			{
				npc.setFollowTarget(st.getPlayer());
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer(), Config.ALT_FOLLOW_RANGE);
			}
			if(DefaultAI.namechar != null)
			{
				if(FollowTask != null)
					FollowTask.cancel(false);
				FollowTask = null;
				FollowTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Follow(npc, player, st), 10, 1000);
			}
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		int state = st.getState();
		int cond = st.getCond();
		if(npcId == 32759)
		{
			if(state == 1)
			{
				if(DefaultAI.namechar != null && DefaultAI.namechar != player.getName())
					return "lost_villager_q0457_01a.htm";
				String req = st.getPlayer().getVar("NextQuest457") == null || st.getPlayer().getVar("NextQuest457").equalsIgnoreCase("null") ? "0" : st.getPlayer().getVar("NextQuest457");
				if(Long.parseLong(req) > System.currentTimeMillis())
					return "lost_villager_q0457_02.htm";
				if(st.getPlayer().getLevel() >= 82)
					return "lost_villager_q0457_01.htm";
				return "lost_villager_q0457_03.htm";
			}
			if(state == 2)
			{
				if(DefaultAI.namechar != null && DefaultAI.namechar != player.getName())
					return "lost_villager_q0457_01a.htm";
				if(cond == 2)
				{
					st.giveItems(15716, 1);
					st.removeMemo("cond");
					st.soundEffect(SOUND_FINISH);
					st.setState(CREATED);
					DefaultAI.namechar = null;
					npc.deleteMe();
					Calendar reDo = Calendar.getInstance();
					reDo.set(Calendar.MINUTE, RESET_MIN);
					if(reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
						reDo.add(Calendar.DATE, 1);
					reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
					st.getPlayer().setVar("NextQuest457", String.valueOf(reDo.getTimeInMillis()), -1);
					return "lost_villager_q0457_09.htm";
				}
				if(cond == 1)
					if(seeSoldier(npc, st.getPlayer()) == null)
					{
						npc.setFollowTarget(st.getPlayer());
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer(), Config.ALT_FOLLOW_RANGE);
					}
					return "lost_villager_q0457_08.htm";
			}
		}
		return "noquest";
	}

	private void checkInRadius(int id, QuestState st, NpcInstance npc)
	{
		NpcInstance quest0457 = GameObjectsStorage.getByNpcId(id);
		if(npc.getRealDistance3D(quest0457) <= 150)
		{
			st.setCond(2);
			if(FollowTask != null)
				FollowTask.cancel(false);
			FollowTask = null;
			npc.stopMove();
		}
	}
	private NpcInstance seeSoldier(NpcInstance npc, Player player)
	{
		List<NpcInstance> around = npc.getAroundNpc(Config.ALT_FOLLOW_RANGE * 2, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance n : around)
				if(n.getNpcId() == Gumiel && n.getFollowTarget() != null)
					if(n.getFollowTarget().getObjectId() == player.getObjectId())
						return n;

		return null;
	}
	private class Follow implements Runnable
	{
		private NpcInstance _npc;
		private Player player;
		private QuestState st;

		private Follow(NpcInstance npc, Player pl, QuestState _st)
		{
			_npc = npc;
			player = pl;
			st = _st;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, 150);
			checkInRadius(32764, st, _npc);
		}
	}
}