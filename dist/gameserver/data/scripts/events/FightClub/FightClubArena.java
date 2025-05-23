package events.FightClub;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerExitListener;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.DoorTemplate;
import jts.gameserver.templates.ZoneTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.ReflectionUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class FightClubArena extends FightClubManager implements OnDeathListener, OnPlayerExitListener
{
	private static final int[] doors = new int[] { 17160020, 17160019, 17160024, 17160023 };
	protected static final String CLASS_NAME = "events.FightClub.FightClubManager";

	private ScheduledFuture<?> _endTask;
	public static ScheduledFuture<?> _startTask;

	private boolean _isEnded = false;
	private Player player1;
	private Player player2;
	private int itemId;
	private int itemCount;
	private Reflection reflection;
	private ZoneListener _zoneListener;
	private Zone _zone;
	private Map<String, ZoneTemplate> _zones;
	private IntObjectMap<DoorTemplate> _doors;

	public FightClubArena(Player player1, Player player2, int itemId, int itemCount, Reflection reflection)
	{
		//Подключаем листенеры персонажа
		CharListenerList.addGlobal(this);

		//Инициализируем переменные класса
		this.player1 = player1;
		this.player2 = player2;
		this.itemId = itemId;
		this.itemCount = itemCount;
		this.reflection = reflection;

		_zoneListener = new ZoneListener();
		_zones = new HashMap<String, ZoneTemplate>();
		_doors = new HashIntObjectMap<DoorTemplate>();

		//Создаём баттл-зону, создаем двери и добавляем листенер 
		_zones.put("[fightclub_battle]", ReflectionUtils.getZone("[fightclub_battle]").getTemplate());

		for(final int doorId : doors)
			_doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());

		reflection.init(_doors, _zones);

		for(final int doorId : doors)
			reflection.getDoor(doorId).openMe();

		_zone = reflection.getZone("[fightclub_battle]");
		_zone.addListener(_zoneListener);

		//Инициализируем сражение
		initBattle();
	}

	/**
	 * Вызывается при выходе игрока
	 */
	@Override
	public void onPlayerExit(Player player)
	{
		if((player.getStoredId() == player1.getStoredId() || player.getStoredId() == player2.getStoredId()) && !_isEnded)
		{
			stopEndTask();
			setLoose(player);
		}
	}

	/**
	 * Вызывается при смерти игрока
	 */
	@Override
	public void onDeath(Creature actor, Creature killer)
	{
		if((actor.getStoredId() == player1.getStoredId() || actor.getStoredId() == player2.getStoredId()) && !_isEnded)
		{
			stopEndTask();
			setLoose((Player) actor);
		}
	}

	private void stopEndTask()
	{
		_endTask.cancel(false);
		_endTask = ThreadPoolManager.getInstance().schedule(new EndTask(), 3000);
	}

	/**
	 * Запускает таймеры боя
	 */
	private void initBattle()
	{
		final Object[] args = { player1, player2, reflection };
		_startTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new StartTask(player1, player2), Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY * 1000, 1000);
		_endTask = ThreadPoolManager.getInstance().schedule(new EndTask(), ((Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY + Config.FIGHT_CLUB_FIGHT_TIME)) * 1000);
		sayToPlayers("scripts.events.fightclub.TeleportThrough", Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY, false, player1, player2);
		executeTask(CLASS_NAME, "resurrectPlayers", args, Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY * 1000 - 600);
		executeTask(CLASS_NAME, "healPlayers", args, Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY * 1000 - 500);
		executeTask(CLASS_NAME, "teleportPlayersToColliseum", args, Config.FIGHT_CLUB_ARENA_TELEPORT_DELAY * 1000);
	}

	/**
	 * Удаляет ауру у игроков
	 */
	private void removeAura()
	{
		player1.setTeam(TeamType.NONE);
		player2.setTeam(TeamType.NONE);
	}

	/**
	 * Выдаёт награду
	 */
	private void giveReward(Player player)
	{
		final String name = ItemFunctions.createItem(itemId).getTemplate().getName();
		sayToPlayer(player, "scripts.events.fightclub.YouWin", false, itemCount * 2, name);
		Functions.addItem(player, itemId, itemCount * 2);
	}

	/**
	 * Выводит скорбящее сообщение проигравшему ;)
	 * @param player
	 */
	private void setLoose(Player player)
	{
		if(player.getStoredId() == player1.getStoredId())
			giveReward(player2);
		else if(player.getStoredId() == player2.getStoredId())
			giveReward(player1);
		_isEnded = true;
		sayToPlayer(player, "scripts.events.fightclub.YouLoose", false, new Object[0]);
	}

	/**
	 * Метод, вызываемый при ничьей. Рассчитывает победителя или объявлет ничью.
	 */
	private void draw()
	{
		if(!Config.FIGHT_CLUB_ALLOW_DRAW)
		{
			int player1Heal = (int) (player1.getMaxCp() / player1.getCurrentCp() + player1.getMaxHp() / player1.getCurrentHp());
			int player2Heal = (int) (player2.getMaxCp() / player2.getCurrentCp() + player2.getMaxHp() / player2.getCurrentHp());
			if(player1Heal > player2Heal)
			{
				giveReward(player1);
				setLoose(player2);
				return;
			}
			else
			{
				giveReward(player2);
				setLoose(player1);
				return;
			}
		}
		sayToPlayers("scripts.events.fightclub.Draw", true, player1, player2);
		Functions.addItem(player1, itemId, itemCount);
		Functions.addItem(player2, itemId, itemCount);
	}

	/**
	 * Возващает ссылку на первого игрока
	 * @return - ссылка на игрока
	 */
	protected Player getPlayer1()
	{
		return player1;
	}

	/**
	 * Возващает ссылку на второго игрока
	 * @return - ссылка на игрока
	 */
	protected Player getPlayer2()
	{
		return player2;
	}

	/**
	 * Возвращает отражение
	 * @return - reflection
	 */
	protected Reflection getReflection()
	{
		return reflection;
	}

	/**
	 * Вызывает метод суперкласса, удаляющий рефлекшен
	 * @param arena - ссылка на арену
	 */
	private void delete(long delay)
	{
		final FightClubArena[] arg = { this };
		executeTask(CLASS_NAME, "deleteArena", arg, delay);
	}

	protected static class StartTask extends RunnableImpl
	{

		private Player player1;
		private Player player2;
		private int second;

		public StartTask(Player player1, Player player2)
		{
			this.player1 = player1;
			this.player2 = player2;
			second = Config.FIGHT_CLUB_TIME_TO_PREPARATION;
		}

		@Override
		public void runImpl() throws Exception
		{
			switch(second)
			{
				case 30:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 20:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 10:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 5:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 3:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 2:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 1:
					sayToPlayers("scripts.events.fightclub.TimeToStart", second, false, player1, player2);
					break;
				case 0:
					startBattle(player1, player2);
					_startTask.cancel(true);
					_startTask = null;
			}
			second--;
		}
	}

	private class EndTask extends RunnableImpl
	{
		private final Object[] args = { player1, player2, new Object[0] };

		@Override
		public void runImpl() throws Exception
		{
			removeAura();
			if(!_isEnded)
			{
				draw();
				_isEnded = true;
				stopEndTask();
			}
			sayToPlayers("scripts.events.fightclub.TeleportBack", Config.FIGHT_CLUB_TIME_TELEPORT_BACK, false, player1, player2);
			executeTask(CLASS_NAME, "resurrectPlayers", args, Config.FIGHT_CLUB_TIME_TELEPORT_BACK * 1000 - 300);
			executeTask(CLASS_NAME, "healPlayers", args, Config.FIGHT_CLUB_TIME_TELEPORT_BACK * 1000 - 200);
			executeTask(CLASS_NAME, "teleportPlayersBack", args, Config.FIGHT_CLUB_TIME_TELEPORT_BACK * 1000);
			delete((Config.FIGHT_CLUB_TIME_TELEPORT_BACK + 10) * 1000);
		}

	}

	protected class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null)
				return;

			Player player = actor.getPlayer();
			if(!_inBattle.contains(player.getStoredId()))
				;
			ThreadPoolManager.getInstance().schedule(new TeleportTask(player, new Location(147451, 46728, -3410)), 3000);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			if(actor == null)
				return;

			Player player = actor.getPlayer();
			if(_inBattle.contains(player.getStoredId()))
			{
				double angle = PositionUtils.convertHeadingToDegree(actor.getHeading());
				double radian = Math.toRadians(angle - 90);
				int x = (int) (actor.getX() + 50 * Math.sin(radian));
				int y = (int) (actor.getY() - 50 * Math.cos(radian));
				int z = actor.getZ();
				ThreadPoolManager.getInstance().schedule(new TeleportTask(player, new Location(x, y, z)), 3000);
			}
		}
	}

	public FightClubArena() {}

	@Override
	public void onLoad() {}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}