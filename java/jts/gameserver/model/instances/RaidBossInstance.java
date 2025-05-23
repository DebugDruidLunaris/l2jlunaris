package jts.gameserver.model.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.instancemanager.RaidBossSpawnManager;
import jts.gameserver.model.AggroList.HateInfo;
import jts.gameserver.model.CommandChannel;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectTasks;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.HeroDiary;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class RaidBossInstance extends MonsterInstance
{
	private ScheduledFuture<?> minionMaintainTask;

	private static final int MINION_UNSPAWN_INTERVAL = 5000; //time to unspawn minions when boss is dead, msec

	public RaidBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}

	protected int getKilledInterval(MinionInstance minion)
	{
		return 120000; //2 minutes to respawn
	}

	@Override
	public void notifyMinionDied(MinionInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion));
		super.notifyMinionDied(minion);
	}

	private class MaintainKilledMinion extends RunnableImpl
	{
		private final MinionInstance minion;

		public MaintainKilledMinion(MinionInstance minion)
		{
			this.minion = minion;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(!isDead())
			{
				minion.refreshID();
				spawnMinion(minion);
			}
		}
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}

		final int points = getTemplate().rewardRp;
		if(points > 0)
			calcRaidPointsReward(points);

		if(this instanceof ReflectionBossInstance)
		{
			super.onDeath(killer);
			return;
		}

		if(killer.isPlayable())
		{
			Player player = killer.getPlayer();
			if(player.isInParty())
			{
				for(Player member : player.getParty().getPartyMembers())
					if(member.isNoble())
						Hero.getInstance().addHeroDiary(member.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.getParty().broadCast(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}
			else
			{
				if(player.isNoble())
					Hero.getInstance().addHeroDiary(player.getObjectId(), HeroDiary.ACTION_RAID_KILLED, getNpcId());
				player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			}

			Quest q = QuestManager.getQuest(508);
			if(q != null)
			{
				String qn = q.getName();
				if(player.getClan() != null && player.getClan().getLeader().isOnline() && player.getClan().getLeader().getPlayer().getQuestState(qn) != null)
				{
					QuestState st = player.getClan().getLeader().getPlayer().getQuestState(qn);
					st.getQuest().onKill(this, st);
				}
			}
		}

		if(getMinionList().hasAliveMinions())
			ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
				@Override
				public void runImpl() throws Exception
				{
					if(isDead())
						getMinionList().unspawnMinions();
				}
			}, getMinionUnspawnInterval());

		int boxId = 0;
		switch(getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}

		if(boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if(boxTemplate != null)
			{
				final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());

				ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(box), 60000);
			}
		}

		super.onDeath(killer);
	}

	//FIXME [G1ta0] разобрать этот хлам
	@SuppressWarnings("unchecked")
	private void calcRaidPointsReward(int totalPoints)
	{
		// Object groupkey (L2Party/L2CommandChannel/L2Player) | [List<L2Player> group, Long GroupDdamage]
		Map<Object, Object[]> participants = new HashMap<Object, Object[]>();
		double totalHp = getMaxHp();

		// Разбиваем игроков по группам. По возможности используем наибольшую из доступных групп: Command Channel → Party → StandAlone (сам плюс пет :)
		for(HateInfo ai : getAggroList().getPlayableMap().values())
		{
			Player player = ai.attacker.getPlayer();
			Object key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getPlayer();
			Object[] info = participants.get(key);
			if(info == null)
			{
				info = new Object[] { new HashSet<Player>(), new Long(0) };
				participants.put(key, info);
			}

			// если это пати или командный канал то берем оттуда весь список участвующих, даже тех кто не в аггролисте
			// дубликаты не страшны - это хашсет
			if(key instanceof CommandChannel)
			{
				for(Player p : (CommandChannel) key)
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						((Set<Player>) info[0]).add(p);
			}
			else if(key instanceof Party)
			{
				for(Player p : ((Party) key).getPartyMembers())
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						((Set<Player>) info[0]).add(p);
			}
			else
				((Set<Player>) info[0]).add(player);

			info[1] = ((Long) info[1]).longValue() + ai.damage;
		}

		for(Object[] groupInfo : participants.values())
		{
			Set<Player> players = (HashSet<Player>) groupInfo[0];
			// это та часть, которую игрок заслужил дамагом группы, но на нее может быть наложен штраф от уровня игрока
			int perPlayer = (int) Math.round(totalPoints * ((Long) groupInfo[1]).longValue() / (totalHp * players.size()));
			for(Player player : players)
			{
				int playerReward = perPlayer;
				// применяем штраф если нужен
				playerReward = (int) Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
				if(playerReward == 0)
					continue;
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_RAID_POINTS).addNumber(playerReward));
				RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
			}
		}

		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().onBossDespawned(this);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		addSkill(SkillTable.getInstance().getInfo(4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}