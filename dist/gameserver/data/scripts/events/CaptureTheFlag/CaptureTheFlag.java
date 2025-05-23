package events.CaptureTheFlag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import jts.commons.geometry.Polygon;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerExitListener;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
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
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.Revive;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.skills.AbnormalEffect;
import jts.gameserver.skills.EffectType;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.DoorTemplate;
import jts.gameserver.templates.ZoneTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.ReflectionUtils;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureTheFlag extends Functions implements ScriptFile, OnDeathListener, OnPlayerExitListener
{
	private static final Logger _log = LoggerFactory.getLogger(CaptureTheFlag.class);

	private static ScheduledFuture<?> _startTask;

	private static final int[] doors = new int[] { 24190001, 24190002, 24190003, 24190004 };

	/** <font color=blue>Blue</font> */
	private static List<Long> players_list1 = new CopyOnWriteArrayList<Long>();
	/** <font color=red>Red</font> */
	private static List<Long> players_list2 = new CopyOnWriteArrayList<Long>();

	private static NpcInstance redFlag = null;
	private static NpcInstance blueFlag = null;

	private static Skill buff;

	private static int[][] rewards = new int[Config.EVENT_CTF_REWARDS.length][2];

	private static int[][] mage_buffs;
	private static int[][] fighter_buffs;

	private static boolean _isRegistrationActive = false;
	private static boolean _active = false;
	private static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;

	private static ScheduledFuture<?> _endTask;

	private static Reflection _reflection = ReflectionManager.CTF_EVENT;

	private static Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();
	private static IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap<DoorTemplate>();
	private static Zone _zone;
	private static Zone _blueBaseZone;
	private static Zone _redBaseZone;

	private static BlueBaseZoneListener _blueBaseZoneListener = new BlueBaseZoneListener();
	private static RedBaseZoneListener _redBaseZoneListener = new RedBaseZoneListener();

	private static ZoneListener _zoneListener = new ZoneListener();

	private static Map<Long, Location> _savedCoord = new LinkedHashMap<Long, Location>();

	private static Map<Long, String> boxes = new LinkedHashMap<Long, String>();

	private static Territory team1spawn = new Territory().add(new Polygon().add(149878, 47505).add(150262, 47513).add(150502, 47233).add(150507, 46300).add(150256, 46002).add(149903, 46005).setZmin(-3408).setZmax(-3308));

	private static Territory team2spawn = new Territory().add(new Polygon().add(149027, 46005).add(148686, 46003).add(148448, 46302).add(148449, 47231).add(148712, 47516).add(149014, 47527).setZmin(-3408).setZmax(-3308));

	private static Location blueFlagLoc = new Location(150760, 45848, -3408);
	private static Location redFlagLoc = new Location(148232, 47688, -3408);

	public void onLoad()
	{
		CharListenerList.addGlobal(this);

		_zones.put("[colosseum_battle]", ReflectionUtils.getZone("[colosseum_battle]").getTemplate());
		_zones.put("[colosseum_ctf_blue_base]", ReflectionUtils.getZone("[colosseum_ctf_blue_base]").getTemplate());
		_zones.put("[colosseum_ctf_red_base]", ReflectionUtils.getZone("[colosseum_ctf_red_base]").getTemplate());
		for(final int doorId : doors)
		{
			_doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());
		}
		_reflection.init(_doors, _zones);

		_zone = _reflection.getZone("[colosseum_battle]");
		_blueBaseZone = _reflection.getZone("[colosseum_ctf_blue_base]");
		_redBaseZone = _reflection.getZone("[colosseum_ctf_red_base]");

		_zone.addListener(_zoneListener);
		_blueBaseZone.addListener(_blueBaseZoneListener);
		_redBaseZone.addListener(_redBaseZoneListener);

		_active = ServerVariables.getString("CaptureTheFlag", "off").equalsIgnoreCase("on");

		if(isActive())
		{
			scheduleEventStart();
		}

		if(Config.EVENT_CTF_BUFF_PLAYERS && Config.EVENT_CTF_MAGE_BUFFS.length > 0)
		{
			mage_buffs = new int[Config.EVENT_CTF_MAGE_BUFFS.length][2];
		}

		if(Config.EVENT_CTF_BUFF_PLAYERS && Config.EVENT_CTF_FIGHTER_BUFFS.length > 0)
		{
			fighter_buffs = new int[Config.EVENT_CTF_FIGHTER_BUFFS.length][2];
		}

		int i = 0;

		if(Config.EVENT_CTF_BUFF_PLAYERS && Config.EVENT_CTF_MAGE_BUFFS.length > 0)
		{
			for(String skill : Config.EVENT_CTF_MAGE_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}
		}

		i = 0;

		if(Config.EVENT_CTF_BUFF_PLAYERS && Config.EVENT_CTF_MAGE_BUFFS.length != 0)
		{
			for(String skill : Config.EVENT_CTF_FIGHTER_BUFFS)
			{
				String[] splitSkill = skill.split(",");
				fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
				fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
				i++;
			}
		}

		i = 0;
		if(Config.EVENT_CTF_REWARDS.length != 0)
		{
			for(String reward : Config.EVENT_CTF_REWARDS)
			{
				String[] splitReward = reward.split(",");
				rewards[i][0] = Integer.parseInt(splitReward[0]);
				rewards[i][1] = Integer.parseInt(splitReward[1]);
				i++;
			}
		}

		_log.info("Loaded Event: CaptureTheFlag");
	}

	public void onReload()
	{
		_zone.removeListener(_zoneListener);
		_redBaseZone.removeListener(_redBaseZoneListener);
		_blueBaseZone.removeListener(_blueBaseZoneListener);
		if(_startTask != null)
		{
			_startTask.cancel(true);
		}
	}

	public void onShutdown()
	{
		onReload();
	}

	private static boolean isActive()
	{
		return _active;
	}

	public void activateEvent()
	{
		Player player = (Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			// A ativação do evento se não foi ativado, em seguida, tentar começar. Desde que uma tarefa só começa quando o carregamento
			if(_startTask == null)
			{
				scheduleEventStart();
			}

			ServerVariables.set("CaptureTheFlag", "on");
			_log.info("Evento 'CaptureTheFlag' ativado.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CaptureTheFlag.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Evento 'CaptureTheFlag' foi Ativado.");
		}

		_active = true;

		show("admin/events/events.htm", player);
	}

	public void deactivateEvent()
	{
		Player player = (Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(isActive())
		{
			if(_startTask != null)
			{
				_startTask.cancel(true);
				_startTask = null;
			}
			ServerVariables.unset("CaptureTheFlag");
			_log.info("Event 'CaptureTheFlag' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CaptureTheFlag.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Evento 'CaptureTheFlag' desativado.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public static boolean isRunned()
	{
		return _isRegistrationActive || _status > 0;
	}

	public String DialogAppend_31225(Integer val)
	{
		if(val == 0)
		{
			Player player = (Player) getSelf();
			return HtmCache.getInstance().getNotNull("events/ctf/31225.htm", player);
		}
		return "";
	}

	// RED flag
	public String DialogAppend_35423(Integer val)
	{
		Player player = (Player) getSelf();
		if(player.getTeam() != TeamType.BLUE)
			return "";
		if(val == 0)
			return HtmCache.getInstance().getNotNull("events/ctf/35423.htm", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	// BLUE flag
	public String DialogAppend_35426(Integer val)
	{
		Player player = (Player) getSelf();
		if(player.getTeam() != TeamType.RED)
			return "";
		if(val == 0)
			return HtmCache.getInstance().getNotNull("events/ctf/35426.htm", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	public void capture(String[] var)
	{
		Player player = (Player) getSelf();
		if(var.length != 4)
		{
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		NpcInstance npc = getNpc();

		if(player.isDead() || npc == null || !player.isInRange(npc, 200))
		{
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		Integer base;
		Integer add1;
		Integer add2;
		Integer summ;
		try
		{
			base = Integer.valueOf(var[0]);
			add1 = Integer.valueOf(var[1]);
			add2 = Integer.valueOf(var[2]);
			summ = Integer.valueOf(var[3]);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		if(add1.intValue() + add2.intValue() != summ.intValue())
		{
			player.sendMessage(new CustomMessage("common.Error", player));
			return;
		}

		if(base == 1 && blueFlag.isVisible()) // Base azul
		{
			blueFlag.decayMe();
			addFlag(player, 13561);
		}

		if(base == 2 && redFlag.isVisible()) // Base vermelha
		{
			redFlag.decayMe();
			addFlag(player, 13560);
		}

		if(player.isInvisible())
		{
			player.getEffectList().stopAllSkillEffects(EffectType.Invisible);
		}
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
			_log.info("CaptureTheFlag: start task already running!");
			return;
		}

		Player player = (Player) getSelf();
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
		_time_to_start = Config.EVENT_CTF_TIME;

		players_list1 = new CopyOnWriteArrayList<Long>();
		players_list2 = new CopyOnWriteArrayList<Long>();

		if(redFlag != null)
			redFlag.deleteMe();
		if(blueFlag != null)
			blueFlag.deleteMe();

		redFlag = spawn(redFlagLoc, 35423, _reflection);
		blueFlag = spawn(blueFlagLoc, 35426, _reflection);
		redFlag.decayMe();
		blueFlag.decayMe();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.CaptureTheFlag.AnnouncePreStart", param);

		executeTask("events.CaptureTheFlag.CaptureTheFlag", "question", new Object[0], 10000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "announce", new Object[0], 60000);
		_log.info("CaptureTheFlag: start event [" + _category + "-" + _autoContinue + "]");
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}

	public static void question()
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player != null && !player.isDead() && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().isDefault() && !player.isInOlympiadMode() && !player.isInObserverMode())
			{
				player.scriptRequest(new CustomMessage("scripts.events.CaptureTheFlag.AskPlayer", player).toString(), "events.CaptureTheFlag.CaptureTheFlag:addPlayer", new Object[0]);
			}
		}
	}

	public static void announce()
	{
		if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.CaptureTheFlag.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.CaptureTheFlag.CaptureTheFlag", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.CaptureTheFlag.AnnouncePreStart", param);
			executeTask("events.CaptureTheFlag.CaptureTheFlag", "announce", new Object[0], 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.CaptureTheFlag.AnnounceEventStarting", null);
			executeTask("events.CaptureTheFlag.CaptureTheFlag", "prepare", new Object[0], 5000);
		}
	}

	public void addPlayer()
	{
		Player player = (Player) getSelf();
		if(player == null || !checkPlayer(player, true) || !checkDualBox(player))
			return;

		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(!checkCountTeam(team))
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.MaxCountTeam", player));
			return;
		}

		if(team == 1)
		{
			players_list1.add(player.getStoredId());
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Registered", player));
		}
		else if(team == 2)
		{
			players_list2.add(player.getStoredId());
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Registered", player));
		}
		else
			_log.info("WTF??? Command id 0 in CaptureTheFlag...");
	}

	private static boolean checkCountTeam(int team)
	{
		if(team == 1 && players_list1.size() >= Config.EVENT_CTF_MAX_PLAYER_IN_TEAM)
			return false;
		else if(team == 2 && players_list2.size() >= Config.EVENT_CTF_MAX_PLAYER_IN_TEAM)
			return false;

		return true;
	}

	public static boolean checkPlayer(Player player, boolean first)
	{
		if(first && !_isRegistrationActive)
		{
			player.sendMessage(new CustomMessage("scripts.events.Late", player));
			return false;
		}

		if(first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Cancelled", player));
			if(players_list1.contains(player.getStoredId()))
				players_list1.remove(player.getStoredId());
			if(players_list2.contains(player.getStoredId()))
				players_list2.remove(player.getStoredId());
			if(boxes.containsKey(player.getStoredId()))
				boxes.remove(player.getStoredId());
			return false;
		}

		if(first && player.isDead())
			return false;

		if(first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Cancelled", player));
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Cancelled", player));
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledLevel", player));
			return false;
		}

		if(player.isMounted())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.Cancelled", player));
			return false;
		}

		if(player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledDuel", player));
			return false;
		}

		if(player.getTeam() != TeamType.NONE || player.isInPvPEvent())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledOtherEvent", player));
			return false;
		}

		if(player.isInOlympiadMode() || first && Olympiad.isRegistered(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledOlympiad", player));
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledOtherEvent", player));
			return false;
		}

		if(player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledTeleport", player));
			return false;
		}

		return true;
	}

	public static void prepare()
	{
		closeColiseumDoors();

		for(Zone z : _reflection.getZones())
			z.setType(ZoneType.peace_zone);

		cleanPlayers();
		clearArena();

		redFlag.spawnMe();
		blueFlag.spawnMe();

		executeTask("events.CaptureTheFlag.CaptureTheFlag", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "healPlayers", new Object[0], 2000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "teleportPlayersToColiseum", new Object[0], 4000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "paralyzePlayers", new Object[0], 5000);
		if(Config.EVENT_CTF_BUFF_PLAYERS && Config.EVENT_CTF_FIGHTER_BUFFS.length > 0 && Config.EVENT_CTF_MAGE_BUFFS.length > 0)
		{
			executeTask("events.CaptureTheFlag.CaptureTheFlag", "buffPlayers", new Object[0], 6000);
		}
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "go", new Object[0], 60000);

		sayToAll("scripts.events.CaptureTheFlag.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		clearArena();
		sayToAll("scripts.events.CaptureTheFlag.AnnounceFight", null);
		for(Zone z : _reflection.getZones())
		{
			z.setType(ZoneType.battle_zone);
		}
		_endTask = executeTask("events.CaptureTheFlag.CaptureTheFlag", "endOfTime", new Object[0], (Config.EVENT_CTF_RUNNING_TIME * 60 * 1000));
	}

	public static void endOfTime()
	{
		endBattle(3); // chamar
	}

	public static void endBattle(int win)
	{
		for(Zone z : _reflection.getZones())
		{
			z.setType(ZoneType.peace_zone);
		}

		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}

		removeFlags();

		if(redFlag != null)
		{
			redFlag.deleteMe();
			redFlag = null;
		}

		if(blueFlag != null)
		{
			blueFlag.deleteMe();
			blueFlag = null;
		}

		_status = 0;
		removeAura();

		openColiseumDoors();

		switch(win)
		{
			case 1:
				sayToAll("scripts.events.CaptureTheFlag.AnnounceFinishedRedWins", null);
				giveItemsToWinner(false, true, 1);
				break;
			case 2:
				sayToAll("scripts.events.CaptureTheFlag.AnnounceFinishedBlueWins", null);
				giveItemsToWinner(true, false, 1);
				break;
			case 3:
				sayToAll("scripts.events.CaptureTheFlag.AnnounceFinishedDraw", null);
				giveItemsToWinner(true, true, 0);
				break;
		}

		sayToAll("scripts.events.CaptureTheFlag.AnnounceEnd", null);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "end", new Object[0], 30000);
		_isRegistrationActive = false;
	}

	public static void end()
	{
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "healPlayers", new Object[0], 2000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "teleportPlayersToSavedCoords", new Object[0], 3000);
		executeTask("events.CaptureTheFlag.CaptureTheFlag", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
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
			// se não, tente tempo zashedulit de configs
			scheduleEventStart();
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;

			for(String timeOfDay : Config.EVENT_CTF_START_TIME)
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
			_log.info("CaptureTheFlag: Error figuring out a start time. Check StartTime in config file.");
		}
	}

	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if(team1)
		{
			for(Player player : getPlayers(players_list1))
			{
				for(int i = 0; i < rewards.length; i++)
				{
					addItem(player, rewards[i][0], Math.round((Config.EVENT_CTF_RATE ? player.getLevel() : 1) * rewards[i][1] * rate));
				}
			}
		}
		if(team2)
		{
			for(Player player : getPlayers(players_list2))
			{
				for(int i = 0; i < rewards.length; i++)
				{
					addItem(player, rewards[i][0], Math.round((Config.EVENT_CTF_RATE ? player.getLevel() : 1) * rewards[i][1] * rate));
				}
			}
		}
	}

	public static void teleportPlayersToColiseum()
	{
		for(Player player : getPlayers(players_list1))
		{
			unRide(player);

			if(!Config.EVENT_CTF_ALLOW_SUMMONS)
			{
				unSummonPet(player, true);
			}

			DuelEvent duel = player.getEvent(DuelEvent.class);
			if(duel != null)
			{
				duel.abortDuel(player);
			}

			_savedCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));

			player.teleToLocation(Territory.getRandomLoc(team1spawn), _reflection);
			player.setIsInCtF(true);

			if(!Config.EVENT_CTF_ALLOW_BUFFS)
			{
				player.getEffectList().stopAllEffects();
				if(player.getPet() != null)
				{
					player.getPet().getEffectList().stopAllEffects();
				}
			}
		}

		for(Player player : getPlayers(players_list2))
		{
			unRide(player);

			if(!Config.EVENT_CTF_ALLOW_SUMMONS)
			{
				unSummonPet(player, true);
			}

			_savedCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));

			player.teleToLocation(Territory.getRandomLoc(team2spawn), _reflection);
			player.setIsInCtF(true);

			if(!Config.EVENT_CTF_ALLOW_BUFFS)
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

		for(Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);

			if(!player.isRooted())
			{
				player.startRooted();
				player.startAbnormalEffect(AbnormalEffect.ROOT);
			}
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
		for(Player player : getPlayers(players_list1))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
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

		for(Player player : getPlayers(players_list2))
		{
			if(player.isRooted())
			{
				player.stopRooted();
				player.stopAbnormalEffect(AbnormalEffect.ROOT);
			}
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
		for(Player player : getPlayers(players_list1))
		{
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
		}
		for(Player player : getPlayers(players_list2))
		{
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
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
		{
			if(!checkPlayer(player, false))
			{
				removePlayer(player);
			}
			else
			{
				player.setTeam(TeamType.BLUE);
			}
		}
		for(Player player : getPlayers(players_list2))
		{
			if(!checkPlayer(player, false))
			{
				removePlayer(player);
			}
			else
			{
				player.setTeam(TeamType.RED);
			}
		}
	}

	public static void removeAura()
	{
		for(Player player : getPlayers(players_list1))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInCtF(false);
		}
		for(Player player : getPlayers(players_list2))
		{
			player.setTeam(TeamType.NONE);
			player.setIsInCtF(false);
		}
	}

	/**
	 * чистим арену от мусора
	 */
	public static void clearArena()
	{
		for(GameObject obj : _zone.getObjects())
			if(obj != null)
			{
				Player player = obj.getPlayer();
				if(player != null && !players_list1.contains(player.getStoredId()) && !players_list2.contains(player.getStoredId()))
				{
					player.teleToLocation(147451, 46728, -3410, _reflection);
				}
			}
	}

	public static void resurrectAtBase(Player player)
	{
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
		if(player.getTeam() == TeamType.BLUE)
		{
			player.teleToLocation(Territory.getRandomLoc(team1spawn), _reflection);
		}
		else if(player.getTeam() == TeamType.RED)
		{
			player.teleToLocation(Territory.getRandomLoc(team2spawn), _reflection);
		}
	}

	public static Location OnEscape(Player player)
	{
		if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
		}
		return null;
	}

	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if(cha == null)
				return;
			Player player = cha.getPlayer();
			if(_status > 0 && player != null && !players_list1.contains(player.getStoredId()) && !players_list2.contains(player.getStoredId()))
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
			if(_status > 1 && player != null && player.getTeam() != TeamType.NONE && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // ângulo em graus
				double radian = Math.toRadians(angle - 90); // ângulo em radianos
				int x = (int) (cha.getX() + 250 * Math.sin(radian));
				int y = (int) (cha.getY() - 250 * Math.cos(radian));
				int z = cha.getZ();
				player.teleToLocation(x, y, z, _reflection);
			}
		}
	}

	private static class RedBaseZoneListener implements OnZoneEnterLeaveListener
	{

		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null)
				return;
			Player player = actor.getPlayer();
			if(_status > 0 && player != null && players_list2.contains(player.getStoredId()) && player.isTerritoryFlagEquipped())
			{
				endBattle(1);
			}

		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{}

	}

	private static class BlueBaseZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			if(actor == null)
				return;
			Player player = actor.getPlayer();
			if(_status > 0 && player != null && players_list1.contains(player.getStoredId()) && player.isTerritoryFlagEquipped())
			{
				endBattle(2);
			}
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor) {}

	}

	private static void removePlayer(Player player)
	{
		if(player != null)
		{
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
			player.setIsInCtF(false);
			dropFlag(player);

			if(!Config.EVENT_CTF_ALLOW_MULTI_REGISTER)
			{
				boxes.remove(player.getStoredId());
			}
		}
	}

	private static void addFlag(Player player, int flagId)
	{
		ItemInstance item = ItemFunctions.createItem(flagId);
		item.setCustomType1(77);
		item.setCustomFlags(ItemInstance.FLAG_NO_DESTROY | ItemInstance.FLAG_NO_TRADE | ItemInstance.FLAG_NO_DROP | ItemInstance.FLAG_NO_TRANSFER);
		player.getInventory().addItem(item);
		player.getInventory().equipItem(item);
		player.sendChanges();
		player.sendPacket(Msg.YOU_VE_ACQUIRED_THE_WARD_MOVE_QUICKLY_TO_YOUR_FORCES__OUTPOST);
		player.setCTFflag(true);
	}

	private static void removeFlags()
	{
		for(Player player : getPlayers(players_list1))
		{
			removeFlag(player);
		}
		for(Player player : getPlayers(players_list2))
		{
			removeFlag(player);
		}
	}

	private static void removeFlag(Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() == 77) // 77 é alterada Realidades bandeira Evento
			{
				flag.setCustomFlags(0);
				player.getInventory().destroyItem(flag, 1);
				player.setCTFflag(false);
				player.broadcastUserInfo(true);
			}
		}
	}

	private static void dropFlag(Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() == 77) // 77 Altered Realities Evento esta bandeira
			{
				flag.setCustomFlags(0);
				player.getInventory().destroyItem(flag, 1);
				player.setCTFflag(false);
				player.broadcastUserInfo(true);
				if(flag.getItemId() == 13560)
				{
					redFlag.setXYZ(player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ());
					redFlag.setReflection(_reflection);
					redFlag.spawnMe();
				}
				else if(flag.getItemId() == 13561)
				{
					blueFlag.setXYZ(player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ());
					blueFlag.setReflection(_reflection);
					blueFlag.spawnMe();
				}
			}
		}
	}

	private static List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
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

	private static void openColiseumDoors()
	{
		for(DoorInstance door : _reflection.getDoors())
		{
			door.openMe();
		}
	}

	private static void closeColiseumDoors()
	{
		for(DoorInstance door : _reflection.getDoors())
		{
			door.closeMe();
		}
	}

	@Override
	public void onDeath(Creature self, Creature killer)
	{
		if(_status > 1 && self != null && self.isPlayer() && self.getTeam() != TeamType.NONE && (players_list1.contains(self.getStoredId()) || players_list2.contains(self.getStoredId())))
		{
			dropFlag((Player) self);
			executeTask("events.CaptureTheFlag.CaptureTheFlag", "resurrectAtBase", new Object[] { (Player) self }, 10 * 1000);
		}
	}

	@Override
	public void onPlayerExit(Player player)
	{
		if(player == null || player.getTeam() == TeamType.NONE)
			return;

		// Negligenciado ou retirado durante o registro
		if(_status == 0 && _isRegistrationActive && player.getTeam() != TeamType.NONE && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}

		// Negligenciado ou retirado durante o teletransporte
		if(_status == 1 && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			removePlayer(player);

			player.teleToLocation(_savedCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);

			return;
		}

		// Negligenciado ou retirado durante os eventos
		OnEscape(player);
	}

	public static void mageBuff(Player player)
	{
		if(mage_buffs == null)
			return;

		for(int i = 0; i < mage_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(mage_buffs[i][0], mage_buffs[i][1]);
			buff.getEffects(player, player, false, false);
		}
	}

	public static void fighterBuff(Player player)
	{
		if(fighter_buffs == null)
			return;

		for(int i = 0; i < fighter_buffs.length; i++)
		{
			buff = SkillTable.getInstance().getInfo(fighter_buffs[i][0], fighter_buffs[i][1]);
			buff.getEffects(player, player, false, false);
		}
	}

	private static boolean checkDualBox(Player player)
	{
		if(!Config.EVENT_CTF_ALLOW_MULTI_REGISTER)
		{
			if("IP".equalsIgnoreCase(Config.EVENT_CTF_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getIP()))
				{
					player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledBox", player));
					return false;
				}
			}

			else if("HWid".equalsIgnoreCase(Config.EVENT_CTF_CHECK_WINDOW_METHOD))
			{
				if(boxes.containsValue(player.getNetConnection().getHWID()))
				{
					player.sendMessage(new CustomMessage("scripts.events.CaptureTheFlag.CancelledBox", player));
					return false;
				}
			}
		}
		return true;
	}

	public static void buffPlayers()
	{

		for(Player player : getPlayers(players_list1))
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

		for(Player player : getPlayers(players_list2))
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

	public static void buffPlayer(Player player)
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

	public class StartTask extends RunnableImpl
	{

		@Override
		public void runImpl()
		{
			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("CaptureTheFlag not started: another event is already running");
				return;
			}

			for(Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				{
					_log.debug("CaptureTheFlag not started: CastleSiege in progress");
					return;
				}

			if(Config.EVENT_CTF_CATEGORIES)
			{
				start(new String[] { "1", "1" });
			}
			else
			{
				start(new String[] { "-1", "-1" });
			}
		}
	}

	public static void teleportPlayersToSavedCoords()
	{
		for(Player player : getPlayers(players_list1))
		{
			if(player == null || !_savedCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(_savedCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}
		for(Player player : getPlayers(players_list2))
		{
			if(player == null || !_savedCoord.containsKey(player.getStoredId()))
				continue;
			player.teleToLocation(_savedCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
		}

		_savedCoord.clear();
	}
}