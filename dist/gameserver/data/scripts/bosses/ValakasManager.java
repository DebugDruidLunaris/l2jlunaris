package bosses;

import static jts.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.model.CommandChannel;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.BossInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.PlaySound;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.ReflectionUtils;
import bosses.EpicBossState.State;
import jts.gameserver.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValakasManager extends Functions implements ScriptFile, OnDeathListener
{
	private static final Logger _log = LoggerFactory.getLogger(ValakasManager.class);
	private static final int _teleportCubeLocation[][] = 
	{
		{ 214880, -116144, -1644, 0 },
		{ 213696, -116592, -1644, 0 },
		{ 212112, -116688, -1644, 0 },
		{ 211184, -115472, -1664, 0 },
		{ 210336, -114592, -1644, 0 },
		{ 211360, -113904, -1644, 0 },
		{ 213152, -112352, -1644, 0 },
		{ 214032, -113232, -1644, 0 },
		{ 214752, -114592, -1644, 0 },
		{ 209824, -115568, -1421, 0 },
		{ 210528, -112192, -1403, 0 },
		{ 213120, -111136, -1408, 0 },
		{ 215184, -111504, -1392, 0 },
		{ 215456, -117328, -1392, 0 },
		{ 213200, -118160, -1424, 0 } 
	};

	private static List<NpcInstance> _teleportCube = new ArrayList<NpcInstance>();
	private static List<NpcInstance> _spawnedMinions = new ArrayList<NpcInstance>();
	private static BossInstance _valakas;

	// Tasks.
	private static ScheduledFuture<?> _valakasSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _socialTask = null;
	private static ScheduledFuture<?> _mobiliseTask = null;
	private static ScheduledFuture<?> _moveAtRandomTask = null;
	private static ScheduledFuture<?> _respawnValakasTask = null;
	private static ScheduledFuture<?> _sleepCheckTask = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static final int Valakas = 29028;
	private static final int _teleportCubeId = 31759;
	private static EpicBossState _state;
	private static Zone _zone;

	private static long _lastAttackTime = 0;

	private static final int FWV_LIMITUNTILSLEEP = Config.FIXINTERVALSLEEPVALACAS * 60000;
	private static final int FWV_APPTIMEOFVALAKAS = Config.SPAWN_VALAKAS_TIME * 60000; // 20 minutes
	private static final int FWV_FIXINTERVALOFVALAKAS = Config.FIXINTERVALOFVALAKAS * 60 * 60000;

	private static boolean Dying = false;
	private static final Location TELEPORT_POSITION = new Location(203940, -111840, 66);

	private static boolean _entryLocked = false;

	private static class CheckLastAttack extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			if(_state.getState() == EpicBossState.State.ALIVE)
				if(_lastAttackTime + FWV_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleep();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
		}
	}

	private static class IntervalEnd extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	private static class onAnnihilated extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			sleep();
		}
	}

	private static class SpawnDespawn extends RunnableImpl
	{
		private int _distance = 2550;
		private int _taskId;
		private List<Player> _players = getPlayersInside();

		SpawnDespawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void runImpl() throws Exception
		{
			switch(_taskId)
			{
				case 1:
					// Do spawn.
					_entryLocked = true;
					_valakas = (BossInstance) Functions.spawn(new Location(212852, -114842, -1632, 833), Valakas);

					_valakas.block();
					_valakas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS03_A", 1, _valakas.getObjectId(), _valakas.getLoc()));

					_state.setRespawnDate(Rnd.get(FWV_FIXINTERVALOFVALAKAS, FWV_FIXINTERVALOFVALAKAS));
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(2), 16);
					break;
				case 2:
					// Do social.
					_valakas.broadcastPacket(new SocialAction(_valakas.getObjectId(), 1));

					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1800, 180, -1, 1500, 15000, 0, 0, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(3), 1500);
					break;
				case 3:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 180, -5, 3000, 15000, 0, -5, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(4), 3300);
					break;
				case 4:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 500, 180, -8, 600, 15000, 0, 60, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(5), 2900);
					break;
				case 5:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 800, 180, -8, 2700, 15000, 0, 30, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(6), 2700);
					break;
				case 6:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 200, 250, 70, 0, 15000, 30, 80, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(7), 1);
					break;
				case 7:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1100, 250, 70, 2500, 15000, 30, 80, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(8), 3200);
					break;
				case 8:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 700, 150, 30, 0, 15000, -10, 60, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(9), 1400);
					break;
				case 9:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 150, 20, 2900, 15000, -10, 30, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(10), 6700);
					break;
				case 10:
					// Set camera.
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 750, 170, -10, 3400, 15000, 10, -15, 1, 0);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(11), 5700);
					break;
				case 11:
					// Reset camera.
					for(Player pc : _players)
						pc.leaveMovieMode();

					_valakas.unblock();
					broadcastScreenMessage(NpcString.VALAKAS_ARROGAANT_FOOL_YOU_DARE_TO_CHALLENGE_ME);

					// Move at random.
					if(_valakas.getAI().getIntention() == AI_INTENTION_ACTIVE)
						_valakas.moveToLocation(new Location(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0), 0, false);

					_sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
					break;

				// Death Movie
				case 12:
					_valakas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "B03_D", 1, _valakas.getObjectId(), _valakas.getLoc()));
					broadcastScreenMessage(NpcString.VALAKAS_THE_EVIL_FIRE_DRAGON_VALAKAS_DEFEATED);
					onValakasDie();
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2000, 130, -1, 0, 15000, 0, 0, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(13), 500);
					break;
				case 13:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1100, 210, -5, 3000, 15000, -13, 0, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(14), 3500);
					break;
				case 14:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 200, -8, 3000, 15000, 0, 15, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(15), 4500);
					break;
				case 15:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1000, 190, 0, 500, 15000, 0, 10, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(16), 500);
					break;
				case 16:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 120, 0, 2500, 15000, 12, 40, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(17), 4600);
					break;
				case 17:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 20, 0, 700, 15000, 10, 10, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(18), 750);
					break;
				case 18:
					for(Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1700, 10, 0, 1000, 15000, 20, 70, 1, 1);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(19), 2500);
					break;
				case 19:
					for(Player pc : _players)
					{
						pc.leaveMovieMode();
					}
					break;
			}
		}
	}

	private static void banishForeigners()
	{
		for(Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), 5000);
	}

	private static List<Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	private static int getRespawnInterval()
	{
		return (int) (Config.ALT_RAID_RESPAWN_MULTIPLIER * FWV_FIXINTERVALOFVALAKAS);
	}

	public static Zone getZone()
	{
		return _zone;
	}

	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
			checkAnnihilated();
		else if(self.isNpc() && self.getNpcId() == Valakas)
			ThreadPoolManager.getInstance().schedule(new SpawnDespawn(12), 1);
	}

	private static void onValakasDie()
	{
		if(Dying)
			return;

		Dying = true;
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		_entryLocked = false;
		for(int[] ints : _teleportCubeLocation)
			_teleportCube.add(Functions.spawn(new Location(ints[0], ints[1], ints[2], ints[3]), _teleportCubeId));
		Log.add("Valakas died", "bosses");
	}

	// Start interval.
	private static void setIntervalEndTask()
	{
		setUnspawn();

		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			return;
		}

		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
	}

	// Clean Valakas's lair.
	private static void setUnspawn()
	{
		// Eliminate players.
		banishForeigners();

		_entryLocked = false;

		if(_valakas != null)
			_valakas.deleteMe();

		for(NpcInstance npc : _spawnedMinions)
			npc.deleteMe();

		// Delete teleport cube.
		for(NpcInstance cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();

		if(_valakasSpawnTask != null)
		{
			_valakasSpawnTask.cancel(false);
			_valakasSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(false);
			_mobiliseTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
		}
		if(_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(false);
			_sleepCheckTask = null;
		}
		if(_respawnValakasTask != null)
		{
			_respawnValakasTask.cancel(false);
			_respawnValakasTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
	}

	private static void sleep()
	{
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	// Setting Valakas spawn task.
	public synchronized static void setValakasSpawnTask()
	{
		if(_valakasSpawnTask == null)
			_valakasSpawnTask = ThreadPoolManager.getInstance().schedule(new SpawnDespawn(1), FWV_APPTIMEOFVALAKAS);
	}

	public static boolean isEnableEnterToLair()
	{
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}

	public static void broadcastScreenMessage(NpcString npcs)
	{
		for(Player p : getPlayersInside())
			p.sendPacket(new ExShowScreenMessage(npcs, 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
	}

	public static void addValakasMinion(NpcInstance npc)
	{
		_spawnedMinions.add(npc);
	}

	private void init()
	{
		CharListenerList.addGlobal(this);
		_state = new EpicBossState(Valakas);
		_zone = ReflectionUtils.getZone("[valakas_epic]");
		_log.info("ValakasManager: State of Valakas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
			setIntervalEndTask();
		_log.info("ValakasManager: Next spawn date of Valakas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
	}

	public static void enterTheLair(Player player)
	{
		if(player == null)
			return;

		if(player.getParty() != null && player.getParty().isInCommandChannel())
		{

			CommandChannel cc = player.getParty().getCommandChannel();
			if(cc.getChannelLeader() != player)
			{
				player.sendPacket(Msg.ONLY_THE_ALLIANCE_CHANNEL_LEADER_CAN_ATTEMPT_ENTRY);
				return;
			}
			if(cc.getMemberCount() + getPlayersInside().size() > 200)
			{
				player.sendMessage("Максимум 200 игроков могут вторгнуться в гнездо Валакаса");
				return;
			}
			if(_state.getState() != EpicBossState.State.NOTSPAWN)
			{
				player.sendMessage("Валакаса еще нет, Вы не можете вторгнуться сейчас в гнездо");
				return;
			}
			if(_entryLocked)
			{
				player.sendMessage("Валакас уже проснулся и подвергается нападению.Вход запечатан.");
				return;
			}
			// checking every member of CC for the proper conditions
			if(player.isDead() || player.isMounted() || player.isFlying() || player.isCursedWeaponEquipped())
			{
				player.sendMessage("Вы не отвечаете требованиям, чтобы войти в гнездо");
				return;
			}

			for(Player p : cc)
			{
				if(p.isDead() || p.isFlying() || p.isCursedWeaponEquipped() || !p.isInRange(player, 500))
				{
					player.sendMessage("Участник Командного Канала " + p.getName() + " не отвечает требованиям, чтобы войти в гнездо");
					p.sendMessage("Вы не отвечаете требованиям, чтобы войти в гнездо");
					continue;
				}
				p.teleToLocation(TELEPORT_POSITION);
			}
			setValakasSpawnTask();
		}
		else
		{
			if(getPlayersInside().size() + 1 > 200)
			{
				player.sendMessage("Максимум 200 игроков могут вторгнуться в гнездо Валакаса");
				return;
			}
			if(_state.getState() != EpicBossState.State.NOTSPAWN)
			{
				player.sendMessage("Валакаса еще нет, Вы не можете вторгнуться сейчас в гнездо");
				return;
			}
			if(_entryLocked)
			{
				player.sendMessage("Валакас уже проснулся и подвергается нападению.Вход запечатан.");
				return;
			}
			// checking solo player
			if(player.isDead() || player.isMounted() || player.isFlying() || player.isCursedWeaponEquipped())
			{
				player.sendMessage("Вы не отвечаете требованиям, чтобы войти в гнездо");
				return;
			}

			player.teleToLocation(TELEPORT_POSITION);
			setValakasSpawnTask();
		}
	}

	@Override
	public void onLoad()
	{
		init();
	}

	@Override
	public void onReload()
	{
		sleep();
	}

	@Override
	public void onShutdown() {}
}