package events.DeathMatch;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerExitListener;
import jts.gameserver.listener.actor.player.OnTeleportListener;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Territory;
import jts.gameserver.model.Zone;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import jts.gameserver.network.serverpackets.ExCubeGameChangePoints;
import jts.gameserver.network.serverpackets.ExCubeGameCloseUI;
import jts.gameserver.network.serverpackets.ExCubeGameEnd;
import jts.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import jts.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.Revive;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.skills.AbnormalEffect;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.DoorTemplate;
import jts.gameserver.templates.ZoneTemplate;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.ReflectionUtils;

import org.apache.commons.lang3.mutable.MutableInt;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathMatch extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{

	private static final Logger _log = LoggerFactory.getLogger(DeathMatch.class);

	private static ScheduledFuture<?> _startTask;

	private static final int[] doors = new int[] { 24190001, 24190002, 24190003, 24190004 };

	private static List<Long> players_list1 = new CopyOnWriteArrayList<Long>();
	private static List<Long> players_list2 = new CopyOnWriteArrayList<Long>();
	private static List<Long> live_list1 = new CopyOnWriteArrayList<Long>();
	private static List<Long> live_list2 = new CopyOnWriteArrayList<Long>();

	private static int[][] mage_buffs = new int[Config.EVENT_DEATH_MATCH_MAGE_BUFFS.length][2];
	private static int[][] fighter_buffs = new int[Config.EVENT_DEATH_MATCH_FIGHTER_BUFFS.length][2];
	private static long _startedTime = 0;

	private static int[][] rewards = new int[Config.EVENT_DEATH_MATCH_REWARDS.length][2];

	private static Map<Long, Location> playerRestoreCoord = new LinkedHashMap<Long, Location>();

	private static Map<Long, String> boxes = new LinkedHashMap<Long, String>();

	private static boolean _isRegistrationActive = false;
	private static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;
	private static boolean _active = false;
	private static Skill buff;

	private static Reflection reflection = ReflectionManager.DEATH_MATH_EVENT;

	private static ScheduledFuture<?> _endTask;

	private static Zone _zone;
	private static Zone _zone1;
	private static Zone _myZone = null;
	private static Territory territory = null;
	private static Map<Integer, Integer> _pScore = new HashMap<Integer, Integer>();;
	private static Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();
	private static IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();
	private static ZoneListener _zoneListener = new ZoneListener();

	private static int bluePoints = 0;
	private static int redPoints = 0;
	private static TIntObjectHashMap<MutableInt> score = new TIntObjectHashMap<MutableInt>();

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);

		_zones.put("[cleft_deathmatch]", ReflectionUtils.getZone("[cleft_deathmatch]").getTemplate());

		for(final int doorId : doors)
			_doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());

		int geoIndex = GeoEngine.NextGeoIndex(24, 19, reflection.getId());
		reflection.setGeoIndex(geoIndex);
		reflection.init(_doors, _zones);

		_zone = reflection.getZone("[cleft_deathmatch]");
		_zone1 = reflection.getZone("[cleft_deathmatch]");
		_active = ServerVariables.getString("DeathMatch", "off").equalsIgnoreCase("on");
		if(isActive())
			scheduleEventStart();

		_zone.addListener(_zoneListener);
		_zone1.addListener(_zoneListener);

		int i = 0;

		if(Config.EVENT_DEATH_MATCH_BUFF_PLAYERS && Config.EVENT_DEATH_MATCH_MAGE_BUFFS.length != 0)
			for(String skill : Config.EVENT_DEATH_MATCH_MAGE_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;

		if(Config.EVENT_DEATH_MATCH_BUFF_PLAYERS && Config.EVENT_DEATH_MATCH_MAGE_BUFFS.length != 0)
			for(String skill : Config.EVENT_DEATH_MATCH_FIGHTER_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;
		if(Config.EVENT_DEATH_MATCH_REWARDS.length != 0)
			for(String reward : Config.EVENT_DEATH_MATCH_REWARDS)
			{
				String[] splitReward = reward.split(",");
				rewards[i][0] = Integer.parseInt(splitReward[0]);
				rewards[i][1] = Integer.parseInt(splitReward[1]);
				i++;
			}

		_log.info("Loaded Event: DeathMatch");
	}

	@Override
	public void onReload()
	{
		if(_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	private static long getStarterTime()
	{
		return _startedTime;
	}

	private static boolean isActive()
	{
		return _active;
	}

	public void activateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			if(_startTask == null)
				scheduleEventStart();
			ServerVariables.set("DeathMatch", "on");
			_log.info("Event 'DeathMatch' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.DeathMatch.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'DeathMatch' already active.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	public void deactivateEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(isActive())
		{
			if(_startTask != null)
			{
				_startTask.cancel(false);
				_startTask = null;
			}
			ServerVariables.unset("DeathMatch");
			_log.info("Event 'DeathMatch' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.DeathMatch.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'DeathMatch' not active.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public static boolean isRunned()
	{
		return _isRegistrationActive || _status > 0;
	}

	public static int getMinLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 20;
			case 2:
				return 30;
			case 3:
				return 40;
			case 4:
				return 52;
			case 5:
				return 62;
			case 6:
				return 76;
		}
		return 0;
	}

	public static int getMaxLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 29;
			case 2:
				return 39;
			case 3:
				return 51;
			case 4:
				return 61;
			case 5:
				return 75;
			case 6:
				return 85;
		}
		return 0;
	}

	public static int getCategory(int level)
	{
		if(level >= 20 && level <= 29)
			return 1;
		else if(level >= 30 && level <= 39)
			return 2;
		else if(level >= 40 && level <= 51)
			return 3;
		else if(level >= 52 && level <= 61)
			return 4;
		else if(level >= 62 && level <= 75)
			return 5;
		else if(level >= 76)
			return 6;
		return 0;
	}

	public void start(String[] var)
	{
		Player player = getSelf();
		if(var.length != 2)
		{
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		Integer category;
		Integer autoContinue;
		try
		{
			category = Integer.valueOf(var[0]);
			autoContinue = Integer.valueOf(var[1]);
		}
		catch(Exception e)
		{
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		_category = category;
		_autoContinue = autoContinue;

		if(_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 85;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}

		if(_endTask != null)
		{
			player.sendMessage(new CustomMessage("common.TryLater", player));
			return;
		}

		_status = 0;
		_isRegistrationActive = true;
		_time_to_start = Config.EVENT_DEATH_MATCH_TIME;

		players_list1 = new CopyOnWriteArrayList<Long>();
		players_list2 = new CopyOnWriteArrayList<Long>();
		live_list1 = new CopyOnWriteArrayList<Long>();
		live_list2 = new CopyOnWriteArrayList<Long>();

		playerRestoreCoord = new LinkedHashMap<Long, Location>();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnouncePreStart", param);

		executeTask("events.DeathMatch.DeathMatch", "question", new Object[0], 10000);
		executeTask("events.DeathMatch.DeathMatch", "announce", new Object[0], 60000);
	}

	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode())
				player.scriptRequest(new CustomMessage("scripts.events.DeathMatch.AskPlayer", player).toString(), "events.DeathMatch.DeathMatch:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnouncePreStart", param);
			executeTask("events.DeathMatch.DeathMatch", "announce", new Object[0], 60000);
		}
		else
		{
			if(players_list1.isEmpty() || players_list2.isEmpty() || players_list1.size() < Config.EVENT_DEATH_MATCH_MIN_PLAYER_IN_TEAM || players_list2.size() < Config.EVENT_DEATH_MATCH_MIN_PLAYER_IN_TEAM)
			{
				DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceEventCancelled", null);
				_isRegistrationActive = false;
				_status = 0;
				boxes.clear();
				executeTask("events.DeathMatch.DeathMatch", "autoContinue", new Object[0], 10000);
				return;
			}
			else
			{
				_status = 1;
				_isRegistrationActive = false;
				DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceEventStarting", null);
				executeTask("events.DeathMatch.DeathMatch", "prepare", new Object[0], 5000);
			}
		}
	}

	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(size1 == Config.EVENT_DEATH_MATCH_MAX_PLAYER_IN_TEAM && size2 == Config.EVENT_DEATH_MATCH_MAX_PLAYER_IN_TEAM)
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledCount", player));
			_isRegistrationActive = false;
			return;
		}

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(team == 1)
		{
			players_list1.add(player.getStoredId());
			live_list1.add(player.getStoredId());
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.Registered", player));
		}
		else if(team == 2)
		{
			players_list2.add(player.getStoredId());
			live_list2.add(player.getStoredId());
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.Registered", player));
		}
		else
			_log.info("WTF??? Command id 0 in DeathMatch...");
	}

	public static boolean checkPlayer(Player player, boolean first)
	{

		if(first && (!_isRegistrationActive || player.isDead()))
		{
			player.sendMessage(new CustomMessage("scripts.events.Late", player));
			return false;
		}

		if(first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.Cancelled", player));
			if(players_list1.contains(player.getStoredId()))
				players_list1.remove(player.getStoredId());
			if(players_list2.contains(player.getStoredId()))
				players_list2.remove(player.getStoredId());
			if(live_list1.contains(player.getStoredId()))
				live_list1.remove(player.getStoredId());
			if(live_list2.contains(player.getStoredId()))
				live_list2.remove(player.getStoredId());
			if(boxes.containsKey(player.getStoredId()))
				boxes.remove(player.getStoredId());
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledLevel", player));
			return false;
		}

		if(player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.Cancelled", player));
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.Cancelled", player));
			return false;
		}

		if(player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledDuel", player));
			return false;
		}

		if(player.getTeam() != TeamType.NONE)
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledOtherEvent", player));
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledOlympiad", player));
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledOtherEvent", player));
			return false;
		}

		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("scripts.event.DeathMatch.CancelledObserver", player));
			return false;
		}

		if(player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledTeleport", player));
			return false;
		}
		return true;
	}

	public static void prepare()
	{
		for(DoorInstance door : reflection.getDoors())
			door.openMe();

		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);

		cleanPlayers();
		executeTask("events.DeathMatch.DeathMatch", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.DeathMatch.DeathMatch", "healPlayers", new Object[0], 2000);
		executeTask("events.DeathMatch.DeathMatch", "teleportPlayersToArena", new Object[0], 3000);
		executeTask("events.DeathMatch.DeathMatch", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.DeathMatch.DeathMatch", "buffPlayers", new Object[0], 5000);
		executeTask("events.DeathMatch.DeathMatch", "go", new Object[0], 60000);

		DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceFight", null);
		for(Zone z : reflection.getZones())
			z.setType(ZoneType.battle_zone);
		_endTask = executeTask("events.DeathMatch.DeathMatch", "endBattle", new Object[0], (Config.EVENT_DEATH_MATCH_RUNNING_TIME * 60 * 1000));
		_startedTime = System.currentTimeMillis() + (Config.EVENT_DEATH_MATCH_RUNNING_TIME * 60 * 1000);

		final ExCubeGameChangePoints initialPoints = new ExCubeGameChangePoints(600, bluePoints, redPoints);
		final ExCubeGameCloseUI cui = new ExCubeGameCloseUI();
		ExCubeGameExtendedChangePoints clientSetUp;

		for(Player player : getPlayers(players_list1))
		{
			if(player == null)
				continue;

			score.put(player.getObjectId(), new MutableInt());

			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), false);
			player.setCurrentMp(player.getMaxMp());

			clientSetUp = new ExCubeGameExtendedChangePoints(600, bluePoints, redPoints, isRedTeam(player), player, 0);
			player.sendPacket(clientSetUp);
			player.sendActionFailed();
			player.sendPacket(initialPoints);
			player.sendPacket(cui);
			player.broadcastCharInfo();
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExCubeGameAddPlayer(player, isRedTeam(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExCubeGameAddPlayer(player, isRedTeam(player)));
		}

		for(Player player2 : getPlayers(players_list2))
		{
			if(player2 == null)
				continue;

			score.put(player2.getObjectId(), new MutableInt());

			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp(), false);
			player2.setCurrentMp(player2.getMaxMp());

			clientSetUp = new ExCubeGameExtendedChangePoints(600, bluePoints, redPoints, isRedTeam(player2), player2, 0);
			player2.sendPacket(clientSetUp);
			player2.sendActionFailed();
			player2.sendPacket(initialPoints);
			player2.sendPacket(cui);
			player2.broadcastCharInfo();
			player2.sendPacket(new ExCubeGameAddPlayer(player2, isRedTeam(player2)));
			for(Player player : getPlayers(players_list1))
				player.sendPacket(new ExCubeGameAddPlayer(player2, isRedTeam(player2)));
		}
	}

	public static void endBattle()
	{
		_status = 0;
		removeAura();
		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);
		boxes.clear();

		if(bluePoints > redPoints)
		{
			DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1);
		}
		else if(bluePoints < redPoints)
		{
			DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1);
		}
		else if(bluePoints == redPoints)
		{
			DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceFinishedDraw", null);
			giveItemsToWinner(true, true, 0.5);
		}

		DifferentMethods.sayToAll("scripts.events.DeathMatch.AnnounceEnd", null);
		executeTask("events.DeathMatch.DeathMatch", "end", new Object[0], 30000);
		_isRegistrationActive = false;
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
		boolean _isRedWinner = bluePoints < redPoints ? true : false;
		final ExCubeGameEnd end = new ExCubeGameEnd(_isRedWinner);

		for(Player player : getPlayers(players_list1))
		{
			player.sendPacket(end);
		}

		for(Player player : getPlayers(players_list2))
		{
			player.sendPacket(end);
		}
		bluePoints = 0;
		redPoints = 0;
		_startedTime = 0;
		_myZone = null;
		territory = null;
		score.clear();
	}

	public static void end()
	{
		executeTask("events.DeathMatch.DeathMatch", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.DeathMatch.DeathMatch", "healPlayers", new Object[0], 2000);
		executeTask("events.DeathMatch.DeathMatch", "teleportPlayers", new Object[0], 3000);
		executeTask("events.DeathMatch.DeathMatch", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
		live_list1.clear();
		live_list2.clear();
		players_list1.clear();
		players_list2.clear();

		if(_autoContinue > 0)
		{
			if(_autoContinue >= 6)
			{
				_autoContinue = 0;
				return;
			}
			start(new String[] { "" + (_autoContinue + 1), "" + (_autoContinue + 1) });
		}
		else
			scheduleEventStart();
	}

	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if(team1)
			for(Player player : getPlayers(players_list1))
				for(int i = 0; i < rewards.length; i++)
					addItem(player, rewards[i][0], Math.round((Config.EVENT_DEATH_MATCH_RATE ? player.getLevel() : 1) * rewards[i][1] * rate));
		if(team2)
			for(Player player : getPlayers(players_list2))
				for(int i = 0; i < rewards.length; i++)
					addItem(player, rewards[i][0], Math.round((Config.EVENT_DEATH_MATCH_RATE ? player.getLevel() : 1) * rewards[i][1] * rate));
	}

	public static void teleportPlayersToArena()
	{
		switch(2)
		{
			case 1:
				_myZone = _zone;
				break;
			case 2:
				_myZone = _zone1;
				break;
			default:
				_myZone = _zone;
		}
		territory = _myZone.getTerritory();

		for(Player player : getPlayers(players_list1))
		{
			unRide(player);

			if(!Config.EVENT_DEATH_MATCH_ALLOW_SUMMONS)
				unSummonPet(player, true);

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
				duel.abortDuel(player);

			playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));

			player.teleToLocation(Territory.getRandomLoc(territory), reflection);
			player.setIsInDeathMatch(true);

			if(!Config.EVENT_DEATH_MATCH_ALLOW_BUFFS)
			{
				player.getEffectList().stopAllEffects();
				if(player.getPet() != null)
					player.getPet().getEffectList().stopAllEffects();
			}
		}

		for(Player player : getPlayers(players_list2))
		{
			unRide(player);

			if(!Config.EVENT_DEATH_MATCH_ALLOW_SUMMONS)
				unSummonPet(player, true);

			playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));

			player.teleToLocation(Territory.getRandomLoc(territory), reflection);
			player.setIsInDeathMatch(true);

			if(!Config.EVENT_DEATH_MATCH_ALLOW_BUFFS)
			{
				player.getEffectList().stopAllEffects();
				if(player.getPet() != null)
					player.getPet().getEffectList().stopAllEffects();
			}
		}
	}

	public static void teleportPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(player == null || !playerRestoreCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}

		for(Player player : getPlayers(players_list2))
		{
			if(player == null || !playerRestoreCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}

		playerRestoreCoord.clear();
	}

	public static void paralyzePlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(player == null)
				continue;

			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);

			if(!player.isRooted())
			{
				player.startRooted();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(!player.isInvul())
			{
				player.setIsInvul(true);
				player.startAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}

			if(player.getPet() != null && !player.getPet().isRooted())
			{
				player.getPet().startRooted();
				player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getPet() != null && !player.getPet().isInvul())
			{
				player.getPet().setIsInvul(true);
				player.getPet().startAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}
		}

		for(Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);

			if(!player.isRooted())
			{
				player.startRooted();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(!player.isInvul())
			{
				player.setIsInvul(true);
				player.startAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}

			if(player.getPet() != null && !player.getPet().isRooted())
			{
				player.getPet().startRooted();
				player.getPet().startAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getPet() != null && !player.getPet().isInvul())
			{
				player.getPet().setIsInvul(true);
				player.getPet().startAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}
		}
	}

	public static void upParalyzePlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.isInvul())
			{
				player.setIsInvul(false);
				player.stopAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}

			if(player.getPet() != null && player.getPet().isRooted())
			{
				player.getPet().stopRooted();
				player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getPet() != null && player.getPet().isInvul())
			{
				player.getPet().setIsInvul(false);
				player.getPet().stopAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}
		}

		for(Player player : getPlayers(players_list2))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.isInvul())
			{
				player.setIsInvul(false);
				player.stopAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}

			if(player.getPet() != null && player.getPet().isRooted())
			{
				player.getPet().stopRooted();
				player.getPet().stopAbnormalEffect(AbnormalEffect.ROOT);
			}

			if(player.getPet() != null && player.getPet().isInvul())
			{
				player.getPet().setIsInvul(false);
				player.getPet().stopAbnormalEffect(AbnormalEffect.INVULNERABLE);
			}
		}
	}

	public static void ressurectPlayers()
	{
		for(Player player : getPlayers(players_list1))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
		for(Player player : getPlayers(players_list2))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
	}

	public static void healPlayers()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(Player player : getPlayers(players_list2))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(Player player : getPlayers(players_list1))
			if(!checkPlayer(player, false))
				removePlayer(player);
		for(Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
	}

	public static void checkLive()
	{
		List<Long> new_live_list1 = new CopyOnWriteArrayList<Long>();
		List<Long> new_live_list2 = new CopyOnWriteArrayList<Long>();

		for(Long storeId : live_list1)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				new_live_list1.add(storeId);
		}

		for(Long storeId : live_list2)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				new_live_list2.add(storeId);
		}

		live_list1 = new_live_list1;
		live_list2 = new_live_list2;

		for(Player player : getPlayers(live_list1))
			if(!player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.RED);
			else
				removePlayer(player);

		for(Player player : getPlayers(live_list2))
			if(!player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.BLUE);
			else
				removePlayer(player);

		if(live_list1.size() < 1 || live_list2.size() < 1)
			endBattle();
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(live_list1))
		{
			player.setTeam(TeamType.NONE);
			if(player.getPet() != null)
				player.getPet().setTeam(TeamType.NONE);
			player.setIsInDeathMatch(false);
		}
		for(Player player : getPlayers(live_list2))
		{
			player.setTeam(TeamType.NONE);
			if(player.getPet() != null)
				player.getPet().setTeam(TeamType.NONE);
			player.setIsInDeathMatch(false);
		}
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_status > 1 && self.isPlayer() && self.getTeam() != TeamType.NONE && (live_list1.contains(self.getStoredId()) || live_list2.contains(self.getStoredId())))
		{
			checkKillsAndAnnounce(killer.getPlayer(), self);
			increasePoints(killer);
			resurrectAtBase(self);
			_pScore.remove(self.getPlayer().getObjectId());
		}

	}

	private static void checkKillsAndAnnounce(Player player, Creature self)
	{
		if(player == null || _pScore == null)
			return;
		int score1 = 0;
		if(_pScore.get(player.getObjectId()) != null)
			score1 = _pScore.get(player.getObjectId());
		_pScore.put(player.getObjectId(), score1 + 1);

		MutableInt points = score.get(player.getObjectId());
		points.increment();

		String text = "";

		switch(_pScore.get(player.getObjectId()))
		{
			case 0:
			case 1:
				return;
			case 2:
				text = player.isLangRus() ? player.getName() + ": Двойное убийство!" : player.getName() + ": Double Kill!";
				break;
			case 3:
				text = player.isLangRus() ? player.getName() + ": Череда убийств!" : player.getName() + ": Killing Spree";
				break;
			case 4:
				text = player.isLangRus() ? player.getName() + ": Разбушевался!" : player.getName() + ": Rampage";
				break;
			case 5:
				text = player.isLangRus() ? player.getName() + ": Неудержимый!" : player.getName() + ": Unstoppable";
				break;
			case 6:
				text = player.isLangRus() ? player.getName() + ": Доминирует!" : player.getName() + ": Dominating";
				break;
			case 7:
				text = player.isLangRus() ? player.getName() + ": Божественен!" : player.getName() + ": Godlike";
				break;
			case 8:
				text = player.isLangRus() ? player.getName() + ": Легендарный!" : player.getName() + ": Legendary";
				break;
			case 9:
				text = player.isLangRus() ? player.getName() + ": Мастер арены!" : player.getName() + ": Arena Master";
				break;
			case 10:
				text = player.isLangRus() ? player.getName() + ": Лучший игрок!" : player.getName() + ": Best Player";
				break;
			default:
				text = player.isLangRus() ? player.getName() + " убил " + self : player.getName() + " kill " + self;
				return;
		}
		for(Player player1 : getPlayers(players_list1))
		{
			player1.sendPacket(new ExShowScreenMessage(text, 3000, ScreenMessageAlign.TOP_CENTER, true));
		}

		for(Player player2 : getPlayers(players_list2))
		{
			player2.sendPacket(new ExShowScreenMessage(text, 3000, ScreenMessageAlign.TOP_CENTER, true));
		}
	}

	public static void resurrectAtBase(Creature self)
	{
		Player player = self.getPlayer();
		if(player == null)
			return;
		if(player.getTeam() == TeamType.NONE)
			return;
		if(player.isDead())
		{
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new Revive(player));
			buffPlayer(player);
		}
		player.teleToLocation(Territory.getRandomLoc(territory), reflection);
	}

	public static void buffPlayer(Player player)
	{
		if(player.isMageClass())
			mageBuff(player);
		else
			fighterBuff(player);
	}

	private static void increasePoints(Creature killer)
	{
		Player player = killer.getPlayer();
		if(player == null)
			return;
		if(player.getTeam() == TeamType.BLUE)
			bluePoints++;
		else
			redPoints++;

		int timeLeft = (int) ((getStarterTime() - System.currentTimeMillis()) / 1000);

		if(player.getTeam() == TeamType.RED)
		{
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
		}
		else
		{
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, false, player, getPlayerScore(player)));
			for(Player player1 : getPlayers(players_list1))
				player1.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
			for(Player player2 : getPlayers(players_list2))
				player2.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, bluePoints, redPoints, true, player, getPlayerScore(player)));
		}
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{}

	@Override
	public void onPlayerExit(Player player)
	{
		if(player == null)
			return;

		if(player.getTeam() == TeamType.NONE)
			return;

		if(_status == 0 && _isRegistrationActive && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}

		if(_status == 1 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
			removePlayer(player);
			return;
		}

		if(_status > 1 && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
	}

	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha) {}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha == null)
				return;
			Player player = cha.getPlayer();
			if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // СѓРіРѕР» РІ РіСЂР°РґСѓСЃР°С…
				double radian = Math.toRadians(angle - 90); // СѓРіРѕР» РІ СЂР°РґРёР°РЅР°С…
				int x = (int) (cha.getX() + 250 * Math.sin(radian));
				int y = (int) (cha.getY() - 250 * Math.cos(radian));
				int z = cha.getZ();
				player.teleToLocation(x, y, z, reflection);
			}
		}
	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			//playerRestoreCoord.remove(player.getStoredId());
			player.setIsInDeathMatch(false);

			if(!Config.EVENT_DEATH_MATCH_ALLOW_MULTI_REGISTER)
				boxes.remove(player.getStoredId());

			for(Player player1 : getPlayers(players_list1))
			{
				player1.sendPacket(new ExCubeGameRemovePlayer(player, player.getTeam() == TeamType.RED ? true : false));
			}

			for(Player player2 : getPlayers(players_list2))
			{
				player2.sendPacket(new ExCubeGameRemovePlayer(player, player.getTeam() == TeamType.RED ? true : false));
			}

			player.sendPacket(new ExCubeGameEnd(false));
			player.setTeam(TeamType.NONE);
			player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
		}
	}

	private static List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public static void buffPlayers()
	{

		for(Player player : getPlayers(players_list1))
		{
			if(player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);
		}

		for(Player player : getPlayers(players_list2))
		{
			if(player.isMageClass())
				mageBuff(player);
			else
				fighterBuff(player);
		}
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;

			for(String timeOfDay : Config.EVENT_DEATH_MATCH_START_TIME)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);

				String[] splitTimeOfDay = timeOfDay.split(":");

				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));

				if(testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);

				if(nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;

				if(_startTask != null)
				{
					_startTask.cancel(false);
					_startTask = null;
				}
				_startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), nextStartTime.getTimeInMillis() - System.currentTimeMillis());

			}

			currentTime = null;
			nextStartTime = null;
			testStartTime = null;

		}
		catch(Exception e)
		{
			_log.warn("DeathMatch: Error figuring out a start time. Check DeathMatchEventInterval in config file.");
		}
	}

	public static void mageBuff(Player player)
	{
		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(mage_buffs[i][0], mage_buffs[i][1]);
			if(buff == null)
				return;
			buff.getEffects(player, player, false, false);
		}
	}

	public static void fighterBuff(Player player)
	{
		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(fighter_buffs[i][0], fighter_buffs[i][1]);
			if(buff == null)
				return;
			buff.getEffects(player, player, false, false);
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_DEATH_MATCH_ALLOW_MULTI_REGISTER)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_DEATH_MATCH_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getIP()))
				{
					player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledBox", player));
					return false;
				}
			}
			else if("HWid".equalsIgnoreCase(Config.EVENT_DEATH_MATCH_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getNetConnection().getHWID()))
				{
					player.sendMessage(new CustomMessage("scripts.events.DeathMatch.CancelledBox", player));
					return false;
				}
			}
		}
		return true;
	}

	public class StartTask extends RunnableImpl
	{

		@Override
		public void runImpl()
		{
			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("DeathMatch not started: another event is already running");
				return;
			}

			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("DeathMatch not started: CastleSiege in progress");
					return;
				}

			if(Config.EVENT_DEATH_MATCH_CATEGORIES)
				start(new String[] { "1", "1" });
			else
				start(new String[] { "-1", "-1" });
		}
	}

	/**
	 * @param player
	 * @return Returns personal player score
	 */
	public static int getPlayerScore(Player player)
	{
		MutableInt points = score.get(player.getObjectId());
		return points.intValue();
	}

	private static boolean isRedTeam(Player player)
	{
		if(player.getTeam() == TeamType.RED)
			return true;
		return false;
	}
}