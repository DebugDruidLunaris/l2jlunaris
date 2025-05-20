package events.lastHero;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerExitListener;
import jts.gameserver.listener.actor.player.OnTeleportListener;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
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
import jts.gameserver.network.serverpackets.Revive;
import jts.gameserver.network.serverpackets.components.ChatType;
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

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastHero extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{
	private static final Logger _log = LoggerFactory.getLogger(LastHero.class);

	private static final int[] doors = new int[] { 24190001, 24190002, 24190003, 24190004 };

	private static ScheduledFuture<?> _startTask;

	private static List<Long> players_list = new CopyOnWriteArrayList<Long>();
	private static List<Long> live_list = new CopyOnWriteArrayList<Long>();
	private static int[][] mage_buffs = new int[Config.EVENT_LAST_HERO_MAGE_BUFFS.length][2];
	private static int[][] fighter_buffs = new int[Config.EVENT_LAST_HERO_FIGHTER_BUFFS.length][2];
	private static List<Player> heroList = new CopyOnWriteArrayList<Player>();

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
	private static ScheduledFuture<?> _endTask;

	private static Reflection reflection = ReflectionManager.LAST_HERO;
	private static Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();
	private static IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();

	private static Zone _zone;
	private static ZoneListener _zoneListener = new ZoneListener();

	private static final Location _enter = new Location(149505, 46719, -3417);

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);

		_zones.put("[colosseum_battle]", ReflectionUtils.getZone("[colosseum_battle]").getTemplate());
		for(final int doorId : doors)
			_doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());
		reflection.init(_doors, _zones);
		_zone = reflection.getZone("[colosseum_battle]");
		_zone.addListener(_zoneListener);

		_active = ServerVariables.getString("LastHero", "off").equalsIgnoreCase("on");

		if(isActive())
			scheduleEventStart();

		int i = 0;

		if(Config.EVENT_LAST_HERO_BUFF_PLAYERS && Config.EVENT_LAST_HERO_MAGE_BUFFS.length != 0)
			for(String skill : Config.EVENT_LAST_HERO_MAGE_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		i = 0;

		if(Config.EVENT_LAST_HERO_BUFF_PLAYERS && Config.EVENT_LAST_HERO_FIGHTER_BUFFS.length != 0)
			for(String skill : Config.EVENT_LAST_HERO_FIGHTER_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}

		_log.info("Loaded Event: Last Hero");
	}

	@Override
	public void onReload()
	{
		_zone.removeListener(_zoneListener);
		if(_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}
	}

	@Override
	public void onShutdown()
	{
		for(Player player : heroList)
		{
			if(player != null)
			{
				player.onHero(false);
			}
		}
		onReload();
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
			{
				scheduleEventStart();
			}
			ServerVariables.set("LastHero", "on");
			_log.info("Event 'Last Hero' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'Last Hero' already active.");
		}

		_active = true;

		show("admin/events/events.htm", player);
	}

	public void teleportPlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			if(player == null || !playerRestoreCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}
		playerRestoreCoord.clear();
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
			ServerVariables.unset("LastHero");
			_log.info("Event 'Last Hero' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'LastHero' not active.");

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
		if(isRunned())
		{
			_log.info("LastHero: start task already running!");
			return;
		}

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
		_time_to_start = Config.EVENT_LAST_HERO_TIME;

		players_list = new CopyOnWriteArrayList<Long>();
		live_list = new CopyOnWriteArrayList<Long>();
		playerRestoreCoord = new LinkedHashMap<Long, Location>();
		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.LastHero.AnnouncePreStart", param);

		executeTask("events.lastHero.LastHero", "question", new Object[0], 10000);
		executeTask("events.lastHero.LastHero", "announce", new Object[0], 60000);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode())
				player.scriptRequest(new CustomMessage("scripts.events.LastHero.AskPlayer", player).toString(), "events.lastHero.LastHero:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(players_list.size() < 2)
		{
			sayToAll("scripts.events.LastHero.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.lastHero.LastHero", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.LastHero.AnnouncePreStart", param);
			executeTask("events.lastHero.LastHero", "announce", new Object[0], 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.LastHero.AnnounceEventStarting", null);
			executeTask("events.lastHero.LastHero", "prepare", new Object[0], 5000);
		}
	}

	public void addPlayer()
	{
		Player player = getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		players_list.add(player.getStoredId());
		live_list.add(player.getStoredId());

		player.sendMessage(new CustomMessage("scripts.events.LastHero.Registered", player));
	}

	public static boolean checkPlayer(Player player, boolean first)
	{
		if(first && (!_isRegistrationActive || player.isDead()))
		{
			player.sendMessage(new CustomMessage("scripts.events.Late", player));
			return false;
		}

		if(first && players_list.contains(player.getStoredId()))
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
			if(players_list.contains(player.getStoredId()))
				players_list.remove(player.getStoredId());
			if(live_list.contains(player.getStoredId()))
				live_list.remove(player.getStoredId());
			if(boxes.containsKey(player.getStoredId()))
				boxes.remove(player.getStoredId());
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledLevel", player));
			return false;
		}

		if(player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.Cancelled", player));
			return false;
		}

		if(player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledDuel", player));
			return false;
		}

		if(player.getTeam() != TeamType.NONE || player.isInPvPEvent())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player));
			return false;
		}

		if(player.getOlympiadGame() != null || first && Olympiad.isRegistered(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOlympiad", player));
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player));
			return false;
		}

		if(player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledTeleport", player));
			return false;
		}

		if(player.isInObserverMode())
		{
			player.sendMessage(new CustomMessage("scripts.event.LastHero.CancelledObserver", player));
			return false;
		}
		if(!Config.EVENT_LAST_HERO_ALLOW_HEROES && player.isHero())
		{
			player.sendMessage(new CustomMessage("scripts.event.LastHero.CancelledHero", player));
			return false;
		}

		return true;
	}

	public static void prepare()
	{

		for(DoorInstance door : reflection.getDoors())
			door.closeMe();

		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);

		cleanPlayers();
		clearArena();

		executeTask("events.lastHero.LastHero", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.lastHero.LastHero", "healPlayers", new Object[0], 2000);
		executeTask("events.lastHero.LastHero", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.lastHero.LastHero", "teleportPlayersToColiseum", new Object[0], 3000);
		executeTask("events.lastHero.LastHero", "buffPlayers", new Object[0], 5000);
		executeTask("events.lastHero.LastHero", "go", new Object[0], 60000);

		sayToAll("scripts.events.LastHero.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.LastHero.AnnounceFight", null);
		for(Zone z : reflection.getZones())
			z.setType(ZoneType.battle_zone);
		_endTask = executeTask("events.lastHero.LastHero", "endBattle", new Object[0], (Config.EVENT_LAST_HERO_RUNNING_TIME * 60 * 1000));
	}

	public static void endBattle()
	{
		_status = 0;
		removeAura();

		for(Zone z : reflection.getZones())
			z.setType(ZoneType.peace_zone);
		boxes.clear();
		if(live_list.size() == 1)
			for(Player player : getPlayers(live_list))
			{
				String[] repl = { player.getName() };
				sayToAll("scripts.events.LastHero.AnnounceWiner", repl);
				if(Config.EVENT_LAST_HERO_GIVE_ITEM_FINAL)
				{
					addItem(player, Config.EVENT_LAST_HERO_ITEM_ID_FINAL, Math.round(Config.EVENT_LAST_HERO_RATE_FINAL ? player.getLevel() * Config.EVENT_LAST_HERO_ITEM_COUNT_FINAL : 1 * Config.EVENT_LAST_HERO_ITEM_COUNT_FINAL));
				}
				if(Config.EVENT_LAST_HERO_AURA_ENABLE)
				{
					if(!heroList.contains(player))
					{
						heroList.add(player);
					}
					if(!player.isHero())
					{
						player.setHero(true);
					}
					player.broadcastCharInfo();
				}
				break;
			}
		sayToAll("scripts.events.LastHero.AnnounceEnd", null);
		executeTask("events.lastHero.LastHero", "end", new Object[0], 30000);
		_isRegistrationActive = false;
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
	}

	public static void end()
	{
		executeTask("events.lastHero.LastHero", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.lastHero.LastHero", "healPlayers", new Object[0], 2000);
		executeTask("events.lastHero.LastHero", "teleportPlayers", new Object[0], 3000);
		executeTask("events.lastHero.LastHero", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
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

	public static void teleportPlayersToColiseum()
	{
		for(Player player : getPlayers(players_list))
		{

			unRide(player);
			if(!Config.EVENT_LAST_HERO_ALLOW_SUMMONS)
			{
				unSummonPet(player, true);
			}

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
			{
				duel.abortDuel(player);
			}

			playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));
			player.teleToLocation(Location.findPointToStay(_enter, 150, 500, ReflectionManager.DEFAULT.getGeoIndex()), reflection);
			player.setIsInLastHero(true);
			if(!Config.EVENT_LAST_HERO_ALLOW_BUFFS)
			{
				player.getEffectList().stopAllEffects();
				if(player.getPet() != null)
				{
					player.getPet().getEffectList().stopAllEffects();
				}
			}
		}
	}

	public static void paralyzePlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			if(player == null)
				continue;

			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);

			if(!player.isRooted())
			{
				player.startRooted();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}
            player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
            player.startParalyzed();
            if (player.getPet() != null) {
                player.getPet().startParalyzed();
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
		for(Player player : getPlayers(players_list))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
            player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
            player.stopParalyzed();
            if (player.getPet() != null) {
                player.getPet().stopParalyzed();
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
		for(Player player : getPlayers(players_list))
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
		for(Player player : getPlayers(players_list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(Player player : getPlayers(players_list))
		{
			if(!checkPlayer(player, false))
			{
				removePlayer(player);
			}
		}
	}

	public static void checkLive()
	{
		List<Long> new_live_list = new CopyOnWriteArrayList<Long>();

		for(Long storeId : live_list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				new_live_list.add(storeId);
		}

		live_list = new_live_list;

		for(Player player : getPlayers(live_list))
			if(player.isInZone(_zone) && !player.isDead() && !player.isLogoutStarted())
				player.setTeam(TeamType.RED);
			else
				loosePlayer(player);

		if(live_list.size() <= 1)
			endBattle();
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(live_list))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInLastHero(false);
		}
	}

	public static void clearArena()
	{
		for(GameObject obj : _zone.getObjects())
			if(obj != null)
			{
				Player player = obj.getPlayer();
				if(player != null && !live_list.contains(player.getStoredId()))
					player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
			}
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_status > 1 && self.isPlayer() && self.getTeam() != TeamType.NONE && live_list.contains(self.getStoredId()))
		{
			Player player = (Player) self;
			loosePlayer(player);
			checkLive();
			if(Config.EVENT_LAST_HERO_GIVE_ITEM)
			{
				if(killer != null && killer.isPlayer() && killer.getPlayer().expertiseIndex - player.expertiseIndex > 2 && !killer.getPlayer().getIP().equals(player.getIP()))
				{
					addItem((Player) killer, Config.EVENT_LAST_HERO_ITEM_ID, Math.round(Config.EVENT_LAST_HERO_RATE ? player.getLevel() * Config.EVENT_LAST_HERO_ITEM_COUNT : 1 * Config.EVENT_LAST_HERO_ITEM_COUNT));
				}
			}

			if(self != null)
				self.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);

			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
			
			self.getPlayer().setIsInLastHero(false);
		}
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		if(_zone.checkIfInZone(x, y, z, reflection))
			return;

		if(_status > 1 && player.getTeam() != TeamType.NONE && live_list.contains(player.getStoredId()))
		{
			removePlayer(player);
			checkLive();
		}
	}

	@Override
	public void onPlayerExit(Player player)
	{
		for(Player players : heroList)
		{
			if(players != null)
			{
				players.onHero(false);
			}
		}

		if(player.getTeam() == TeamType.NONE)
			return;

		// Вышел или вылетел во время регистрации
		if(_status == 0 && _isRegistrationActive && live_list.contains(player.getStoredId()))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_status == 1 && live_list.contains(player.getStoredId()))
		{
			player.teleToLocation(playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время ивента
		if(_status > 1 && player.getTeam() != TeamType.NONE && live_list.contains(player.getStoredId()))
		{
			removePlayer(player);
			checkLive();
		}
	}

	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(cha == null)
				return;
			Player player = cha.getPlayer();
			if(_status > 0 && player != null && !live_list.contains(player.getStoredId()))
			{
				player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			if(cha == null)
				return;
			Player player = cha.getPlayer();
			if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && live_list.contains(player.getStoredId()))
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (cha.getX() + 250 * Math.sin(radian));
				int y = (int) (cha.getY() - 250 * Math.cos(radian));
				int z = cha.getZ();
				player.teleToLocation(x, y, z, reflection);
			}
		}
	}

	private static void loosePlayer(Player player)
	{
		if(player != null)
		{
			live_list.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
			player.sendMessage(new CustomMessage("scripts.events.LastHero.YouLose", player));
		}
	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			live_list.remove(player.getStoredId());
			players_list.remove(player.getStoredId());
			playerRestoreCoord.remove(player.getStoredId());
			player.setIsInLastHero(false);

			if(!Config.EVENT_LAST_HERO_ALLOW_MULTI_REGISTER)
			{
				boxes.remove(player.getStoredId());
			}
			player.setTeam(TeamType.NONE);
		}
	}

	private static List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>(list.size());
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
			{
				result.add(player);
			}
		}
		return result;
	}

	public static void buffPlayers()
	{

		for(Player player : getPlayers(players_list))
		{
			if(player.isMageClass())
			{
				mageBuff(player);
			}
			else
			{
				fighterBuff(player);
			}
		}

		for(Player player : getPlayers(live_list))
		{
			if(player.isMageClass())
			{
				mageBuff(player);
			}
			else
			{
				fighterBuff(player);
			}
		}
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;

			for(String timeOfDay : Config.EVENT_LAST_HERO_START_TIME)
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
			_log.warn("LastHero: Error figuring out a start time. Check StartTime in config file.");
		}
	}

	public static void mageBuff(Player player)
	{
		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(mage_buffs[i][0], mage_buffs[i][1]);
			if(player != null && buff != null)
			{
				buff.getEffects(player, player, false, false);
			}
		}
	}

	public static void fighterBuff(Player player)
	{
		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(fighter_buffs[i][0], fighter_buffs[i][1]);
			if(player != null && buff != null)
			{
				buff.getEffects(player, player, false, false);
			}
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_LAST_HERO_ALLOW_MULTI_REGISTER)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_LAST_HERO_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getIP()))
				{
					player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledBox", player));
					return false;
				}
			}

			else if("HWid".equalsIgnoreCase(Config.EVENT_LAST_HERO_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getNetConnection().getHWID()))
				{
					player.sendMessage(new CustomMessage("scripts.events.LastHero.CancelledBox", player));
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
				_log.info("LastHero not started: another event is already running");
				return;
			}

			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("LastHero not started: CastleSiege in progress");
					return;
				}

			if(Config.EVENT_LAST_HERO_CATEGORIES)
			{
				start(new String[] { "1", "1" });
			}
			else
			{
				start(new String[] { "-1", "-1" });
			}
		}
	}
}