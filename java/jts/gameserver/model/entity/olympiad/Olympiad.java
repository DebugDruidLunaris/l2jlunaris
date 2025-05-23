package jts.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import jts.commons.configuration.ExProperties;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.dao.OlympiadNobleDAO;
import jts.gameserver.instancemanager.OlympiadHistoryManager;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.MultiValueIntegerMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Olympiad
{
	private static final Logger _log = LoggerFactory.getLogger(Olympiad.class);

	public static Map<Integer, StatsSet> _nobles;
	public static Map<Integer, Integer> _noblesRank;
	public static List<StatsSet> _heroesToBe;
	public static List<Integer> _nonClassBasedRegisters = new CopyOnWriteArrayList<Integer>();
	public static MultiValueIntegerMap _classBasedRegisters = new MultiValueIntegerMap();
	public static MultiValueIntegerMap _teamBasedRegisters = new MultiValueIntegerMap();

	//public static final int DEFAULT_POINTS = 50;
	//private static final int WEEKLY_POINTS = 10;
	public static final int TEAM_PARTY_SIZE = 3;

	public static final String OLYMPIAD_HTML_PATH = "olympiad/";

	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String POINTS_PAST = "olympiad_points_past";
	public static final String POINTS_PAST_STATIC = "olympiad_points_past_static";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WIN = "competitions_win";
	public static final String COMP_LOOSE = "competitions_loose";
	public static final String GAME_CLASSES_COUNT = "game_classes_count";
	public static final String GAME_NOCLASSES_COUNT = "game_noclasses_count";
	public static final String GAME_TEAM_COUNT = "game_team_count";

	public static long _olympiadEnd;
	public static long _validationEnd;
	public static int _period;
	public static long _nextWeeklyChange;
	public static int _currentCycle;
	private static long _compEnd;
	private static Calendar _compStart;
	public static boolean _inCompPeriod;
	public static boolean _isOlympiadEnd;

	private static ScheduledFuture<?> _scheduledOlympiadEnd;
	public static ScheduledFuture<?> _scheduledManagerTask;
	public static ScheduledFuture<?> _scheduledWeeklyTask;
	public static ScheduledFuture<?> _scheduledValdationTask;

	public static List<String> _checkIp = new ArrayList<String>();

	public static final boolean checkIP = Config.CHECK_OLYMPIAD_IP;
	public static final boolean checkHWID = Config.CHECK_OLYMPIAD_HWID;

	public static final Stadia[] STADIUMS = new Stadia[Config.OLYMPIAD_STADIAS_COUNT];

	public static OlympiadManager _manager;
	private static List<NpcInstance> _npcs = new ArrayList<NpcInstance>();

	public static void load()
	{
		_nobles = new ConcurrentHashMap<Integer, StatsSet>();
		_currentCycle = ServerVariables.getInt("Olympiad_CurrentCycle", -1);
		_period = ServerVariables.getInt("Olympiad_Period", -1);
		_olympiadEnd = ServerVariables.getLong("Olympiad_End", -1);
		_validationEnd = ServerVariables.getLong("Olympiad_ValdationEnd", -1);
		_nextWeeklyChange = ServerVariables.getLong("Olympiad_NextWeeklyChange", -1);

		ExProperties olympiadProperties = Config.load(Config.OLYMPIAD_DATA_FILE);

		if(_currentCycle == -1)
			_currentCycle = olympiadProperties.getProperty("CurrentCycle", 1);
		if(_period == -1)
			_period = olympiadProperties.getProperty("Period", 0);
		if(_olympiadEnd == -1)
			_olympiadEnd = olympiadProperties.getProperty("OlympiadEnd", 0L);
		if(_validationEnd == -1)
			_validationEnd = olympiadProperties.getProperty("ValdationEnd", 0L);
		if(_nextWeeklyChange == -1)
			_nextWeeklyChange = olympiadProperties.getProperty("NextWeeklyChange", 0L);

		initStadiums();

		OlympiadHistoryManager.getInstance();
		OlympiadNobleDAO.getInstance().select();
		OlympiadDatabase.loadNoblesRank();

		switch(_period)
		{
			case 0:
				if(_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
					OlympiadDatabase.setNewOlympiadEnd();
				else
					_isOlympiadEnd = false;
				break;
			case 1:
				_isOlympiadEnd = true;
				_scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), getMillisToValidationEnd());
				break;
			default:
				_log.warn("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}

		_log.info("Olympiad System: Loading Olympiad System....");
		if(_period == 0)
			_log.info("Olympiad System: Currently in Olympiad Period");
		else
			_log.info("Olympiad System: Currently in Validation Period");

		_log.info("Olympiad System: Period Ends....");

		long milliToEnd;
		if(_period == 0)
			milliToEnd = getMillisToOlympiadEnd();
		else
			milliToEnd = getMillisToValidationEnd();

		double numSecs = milliToEnd / 1000 % 60;
		double countDown = (milliToEnd / 1000 - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);

		_log.info("Olympiad System: Em " + numDays + " dias, " + numHours + " horas e " + numMins + " minutos.");

		if(_period == 0)
		{
			_log.info("Olympiad System: Next Weekly Change is in....");

			milliToEnd = getMillisToWeekChange();

			double numSecs2 = milliToEnd / 1000 % 60;
			double countDown2 = (milliToEnd / 1000 - numSecs2) / 60;
			int numMins2 = (int) Math.floor(countDown2 % 60);
			countDown2 = (countDown2 - numMins2) / 60;
			int numHours2 = (int) Math.floor(countDown2 % 24);
			int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24);

			_log.info("Olympiad System: Em " + numDays2 + " dias, " + numHours2 + " horas e " + numMins2 + " minutos.");
		}

		_log.info("Olympiad System: " + _nobles.size() + " Noblesses");

		if(_period == 0)
			init();
	}
	public static void cancelPeriodTasks()
	{
		if(_scheduledValdationTask != null)
			_scheduledValdationTask.cancel(false);
		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);
	}
	private static void initStadiums()
	{
		for(int i = 0; i < STADIUMS.length; i++)
			if(STADIUMS[i] == null)
				STADIUMS[i] = new Stadia();
	}

	public static void init()
	{
		if(_period == 1)
			return;

		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.OLYMPIAD_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.OLYMPIAD_MIN);
		_compEnd = _compStart.getTimeInMillis() + Config.OLYMPIAD_CPERIOD;

		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(false);
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());

		updateCompStatus();

		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(false);
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WeeklyTask(), getMillisToWeekChange(), Config.OLYMPIAD_WPERIOD);
	}

	public static synchronized boolean registerNoble(Player noble, CompType type)
	{
		if(noble.getClassId().getLevel() != 4){ return false; }

		if(!_inCompPeriod || _isOlympiadEnd)
		{
			noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(getMillisToOlympiadEnd() <= 600 * 1000)
		{
			noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(getMillisToCompEnd() <= 600 * 1000)
		{
			noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(noble.isCursedWeaponEquipped())
		{
			noble.sendPacket(SystemMsg.YOU_CANNOT_REGISTER_WHILE_IN_POSSESSION_OF_A_CURSED_WEAPON);
			return false;
		}
		if(noble.isMounted())
		{
			noble.sendMessage("Невозможно зарегистрироватся сидя на питомце ");
			return false;
		}
		if(noble.isInDuel() || noble.getIsInDuel())
		{
			noble.sendMessage(new CustomMessage("jts.gameserver.model.entity.Olympiad.Duel", noble));
			return false;
		}

		if(!HwidChecker.canRegister(noble))
		{
			noble.sendMessage(new CustomMessage("jts.gameserver.model.entity.Olympiad.CheckHwid", noble));
			return false;
		}

		StatsSet nobleInfo = _nobles.get(noble.getObjectId());

		if(!validPlayer(noble, noble, type))
			return false;

		if(getNoblePoints(noble.getObjectId()) < 3)
		{
			noble.sendMessage(new CustomMessage("jts.gameserver.model.entity.Olympiad.LessPoints", noble));
			return false;
		}

		if(noble.getOlympiadGame() != null){ return false; }

		if(_checkIp != null && _checkIp.contains(noble.getIP()) && Config.CHECK_OLYMPIAD_IP)
		{
			noble.sendMessage(new CustomMessage("jts.gameserver.model.entity.Olympiad.CheckIp", noble));
			return false;
		}

		int classId = nobleInfo.getInteger(CLASS_ID);

		switch(type)
		{
			case CLASSED:
			{
				_classBasedRegisters.put(classId, noble.getObjectId());

				if(checkIP)
					_checkIp.add(noble.getIP());

				if(checkHWID)
					HwidChecker.registerPlayer(noble);

				noble.sendPacket(SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_THE_GRAND_OLYMPIAD_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
				break;
			}
			case NON_CLASSED:
			{
				_nonClassBasedRegisters.add(noble.getObjectId());

				if(checkIP)
					_checkIp.add(noble.getIP());

				if(checkHWID)
					HwidChecker.registerPlayer(noble);

				noble.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_REGISTERED_FOR_A_1V1_CLASS_IRRELEVANT_MATCH);
				break;
			}
			case TEAM:
			{
				Party party = noble.getParty();
				if(party == null)
				{
					noble.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_REQUEST_A_TEAM_MATCH);
					return false;
				}

				if(party.getMemberCount() != TEAM_PARTY_SIZE)
				{
					noble.sendPacket(SystemMsg.THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET);
					return false;
				}

				for(Player member : party.getPartyMembers())
				{
					if(!validPlayer(noble, member, type)){ return false; }
				}

				if(checkIP)
					_checkIp.add(noble.getIP());

				if(checkHWID)
					HwidChecker.registerPlayer(noble);

				_teamBasedRegisters.putAll(noble.getObjectId(), party.getPartyMembersObjIds());
				noble.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_REGISTERED_FOR_A_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH);
				break;
			}
		}

		return true;
	}

	private static boolean validPlayer(Player sendPlayer, Player validPlayer, CompType type)
	{
		if(!validPlayer.isNoble())
		{
			sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_ONLY_NOBLESSE_CHARACTERS_CAN_PARTICIPATE_IN_THE_OLYMPIAD).addName(validPlayer));
			return false;
		}

		if(validPlayer.getBaseClassId() != validPlayer.getClassId().getId())
		{
			sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(validPlayer));
			return false;
		}

		int[] ar = getWeekGameCounts(validPlayer.getObjectId());

		switch(type)
		{
			case CLASSED:
				if(_classBasedRegisters.containsValue(validPlayer.getObjectId()))
				{
					sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST).addName(validPlayer));
					return false;
				}

				if(ar[1] == 0)
				{
					validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
					return false;
				}
				break;
			case NON_CLASSED:
				if(_nonClassBasedRegisters.contains(validPlayer.getObjectId()))
				{
					sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_CLASS_IRRELEVANT_INDIVIDUAL_MATCH).addName(validPlayer));
					return false;
				}
				if(ar[2] == 0)
				{
					validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
					return false;
				}
				break;
			case TEAM:
				if(_teamBasedRegisters.containsValue(validPlayer.getObjectId()))
				{
					sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH).addName(validPlayer));
					return false;
				}
				if(ar[3] == 0)
				{
					validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
					return false;
				}
				break;
		}

		if(ar[0] == 0)
		{
			validPlayer.sendPacket(SystemMsg.THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70);
			return false;
		}

		if(isRegisteredInComp(validPlayer))
		{
			sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST).addName(validPlayer));
			return false;
		}

		return true;
	}

	public static synchronized void logoutPlayer(Player player)
	{
		_classBasedRegisters.removeValue(player.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(player.getObjectId()));
		_teamBasedRegisters.removeValue(player.getObjectId());

		if(checkIP)
			_checkIp.remove(player.getIP());

		if(checkHWID)
			HwidChecker.unregisterPlayer(player);

		OlympiadGame game = player.getOlympiadGame();
		if(game != null)
			try
			{
				if(!game.logoutPlayer(player) && !game.validated)
					game.endGame(20000, true);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
	}

	public static synchronized boolean unRegisterNoble(Player noble)
	{
		if(!_inCompPeriod || _isOlympiadEnd)
		{
			noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!noble.isNoble())
		{
			noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!isRegistered(noble))
		{
			noble.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_REGISTERED_FOR_THE_GRAND_OLYMPIAD);
			return false;
		}

		OlympiadGame game = noble.getOlympiadGame();
		if(game != null)
		{
			if(game.getStatus() == BattleStatus.Begin_Countdown)
			{
				// TODO: System Message
				//TODO [VISTALL] узнать ли прерывается бой и если так ли это та мессага SystemMsg.YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED
				noble.sendMessage("Now you can't cancel participation in the Grand Olympiad.");
				return false;
			}

			try
			{
				if(!game.logoutPlayer(noble) && !game.validated)
					game.endGame(20000, true);
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
		_classBasedRegisters.removeValue(noble.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(noble.getObjectId()));
		_teamBasedRegisters.removeValue(noble.getObjectId());

		if(checkIP)
			_checkIp.remove(noble.getIP());

		if(checkHWID)
			HwidChecker.unregisterPlayer(noble);

		noble.sendPacket(SystemMsg.YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_WAITING_LIST);

		return true;
	}

	private static synchronized void updateCompStatus()
	{
		long milliToStart = getMillisToCompBegin();
		double numSecs = milliToStart / 1000 % 60;
		double countDown = (milliToStart / 1000 - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);

		_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		_log.info("Olympiad System: Event starts/started: " + _compStart.getTime());

		ThreadPoolManager.getInstance().schedule(new CompStartTask(), getMillisToCompBegin());
	//TODO Должна быть еще мессага в 23.50 о том что регистрация на олик уже закрыта 1919 мессага//точной инфы нет но пакет есть и нужно задействовать
		
	}

	private static long getMillisToOlympiadEnd()
	{
		return _olympiadEnd - System.currentTimeMillis();
	}

	static long getMillisToValidationEnd()
	{
		if(_validationEnd > System.currentTimeMillis())
			return _validationEnd - System.currentTimeMillis();
		return 10L;
	}

	public static boolean isOlympiadEnd()
	{
		return _isOlympiadEnd;
	}

	public static boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private static long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
			return 10L;
		if(_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		return setNewCompBegin();
	}

	private static long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.OLYMPIAD_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.OLYMPIAD_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + Config.OLYMPIAD_CPERIOD;

		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public static long getMillisToCompEnd()
	{
		return _compEnd - Calendar.getInstance().getTimeInMillis();
	}

	private static long getMillisToWeekChange()
	{
		if(_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
			return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
		return 10L;
	}

	public static synchronized void doWeekTasks()
	{
		if(_period == 1)
			return;
		for(Map.Entry<Integer, StatsSet> entry : _nobles.entrySet())
		{
			StatsSet set = entry.getValue();
			Player player = GameObjectsStorage.getPlayer(entry.getKey());

			if(_period != 1)
				set.set(POINTS, set.getInteger(POINTS) + Config.OLYMPIAD_POINTS_WEEKLY);
			set.set(GAME_CLASSES_COUNT, 0);
			set.set(GAME_NOCLASSES_COUNT, 0);
			set.set(GAME_TEAM_COUNT, 0);

			if(player != null)
				player.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES).addName(player).addInteger(Config.OLYMPIAD_POINTS_WEEKLY));
		}
	}

	public static int getCurrentCycle()
	{
		return _currentCycle;
	}

	public static synchronized void addSpectator(int id, Player spectator)
	{
		if(spectator.getOlympiadGame() != null || isRegistered(spectator) || Olympiad.isRegisteredInComp(spectator))
		{
			spectator.sendPacket(SystemMsg.YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST);
			return;
		}

		if(_manager == null || _manager.getOlympiadInstance(id) == null || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begining || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begin_Countdown)
		{
			spectator.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}

		if(spectator.getPet() != null)
			spectator.getPet().unSummon();

		OlympiadGame game = getOlympiadGame(id);
		List<Location> spawns = game.getReflection().getInstancedZone().getTeleportCoords();
		if(spawns.size() < 3)
		{
			Location c1 = spawns.get(0);
			Location c2 = spawns.get(1);
			spectator.enterOlympiadObserverMode(new Location((c1.x + c2.x) / 2, (c1.y + c2.y) / 2, (c1.z + c2.z) / 2), game, game.getReflection());
		}
		else
			spectator.enterOlympiadObserverMode(spawns.get(2), game, game.getReflection());
	}

	public static synchronized void removeSpectator(int id, Player spectator)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return;

		_manager.getOlympiadInstance(id).removeSpectator(spectator);
	}

	public static List<Player> getSpectators(int id)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return null;
		return _manager.getOlympiadInstance(id).getSpectators();
	}

	public static OlympiadGame getOlympiadGame(int gameId)
	{
		if(_manager == null || gameId < 0)
			return null;
		return _manager.getOlympiadGames().get(gameId);
	}

	public static synchronized int[] getWaitingList()
	{
		if(!inCompPeriod())
			return null;

		int[] array = new int[3];
		array[0] = _classBasedRegisters.totalSize();
		array[1] = _nonClassBasedRegisters.size();
		array[2] = _teamBasedRegisters.totalSize();

		return array;
	}

	public static synchronized int getNoblessePasses(Player player)
	{
		int objId = player.getObjectId();

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;

		int points = noble.getInteger(POINTS_PAST);
		if(points == 0) // Уже получил бонус
			return 0;

		int rank = _noblesRank.get(objId);
		switch(rank)
		{
			case 1:
				points = Config.OLYMPIAD_RANK1_POINTS;
				break;
			case 2:
				points = Config.OLYMPIAD_RANK2_POINTS;
				break;
			case 3:
				points = Config.OLYMPIAD_RANK3_POINTS;
				break;
			case 4:
				points = Config.OLYMPIAD_RANK4_POINTS;
				break;
			default:
				points = Config.OLYMPIAD_RANK5_POINTS;
		}

		if(player.isHero() || Hero.getInstance().isInactiveHero(player.getObjectId()))
			points += Config.OLYMPIAD_HERO_POINTS;

		noble.set(POINTS_PAST, 0);
		OlympiadDatabase.saveNobleData(objId);

		return points * Config.OLYMPIAD_GP_PER_POINT;
	}

	public static synchronized boolean isRegistered(Player noble)
	{
		if(_classBasedRegisters.containsValue(noble.getObjectId()))
			return true;
		if(_nonClassBasedRegisters.contains(noble.getObjectId()))
			return true;
		if(_teamBasedRegisters.containsValue(noble.getObjectId()))
			return true;
		return false;
	}

	public static synchronized boolean isRegisteredInComp(Player player)
	{
		if(isRegistered(player))
			return true;
		if(_manager == null || _manager.getOlympiadGames() == null)
			return false;
		for(OlympiadGame g : _manager.getOlympiadGames().values())
			if(g != null && g.isRegistered(player.getObjectId()))
				return true;
		return false;
	}

	/**
	 * Возвращает олимпийские очки за текущий период
	 * @param objId
	 * @return
	 */
	public static synchronized int getNoblePoints(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(POINTS);
	}

	/**
	 * Возвращает олимпийские очки за прошлый период
	 * @param objId
	 * @return
	 */
	public static synchronized int getNoblePointsPast(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(POINTS_PAST);
	}

	public static synchronized int getCompetitionDone(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_DONE);
	}

	public static synchronized int getCompetitionWin(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_WIN);
	}

	public static synchronized int getCompetitionLoose(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_LOOSE);
	}

	public static synchronized int[] getWeekGameCounts(int objId)
	{
		int[] ar = new int[4];

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return ar;

		ar[0] = Config.OLYMPIAD_GAME_MAX_LIMIT - noble.getInteger(GAME_CLASSES_COUNT) - noble.getInteger(GAME_NOCLASSES_COUNT) - noble.getInteger(GAME_TEAM_COUNT);
		ar[1] = Config.OLYMPIAD_GAME_CLASSES_COUNT_LIMIT - noble.getInteger(GAME_CLASSES_COUNT);
		ar[2] = Config.OLYMPIAD_GAME_NOCLASSES_COUNT_LIMIT - noble.getInteger(GAME_NOCLASSES_COUNT);
		ar[3] = Config.OLYMPIAD_GAME_TEAM_COUNT_LIMIT - noble.getInteger(GAME_TEAM_COUNT);

		return ar;
	}

	public static Stadia[] getStadiums()
	{
		return STADIUMS;
	}

	public static List<NpcInstance> getNpcs()
	{
		return _npcs;
	}

	public static void addOlympiadNpc(NpcInstance npc)
	{
		_npcs.add(npc);
	}

	public static void changeNobleName(int objId, String newName)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;
		noble.set(CHAR_NAME, newName);
		OlympiadDatabase.saveNobleData(objId);
	}

	public static String getNobleName(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return null;
		return noble.getString(CHAR_NAME);
	}

	public static int getNobleClass(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(CLASS_ID);
	}

	public static void manualSetNoblePoints(int objId, int points)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;
		noble.set(POINTS, points);
		OlympiadDatabase.saveNobleData(objId);
	}

	public static synchronized boolean isNoble(int objId)
	{
		return _nobles.get(objId) != null;
	}

	public static synchronized void addNoble(Player noble)
	{
		if(!_nobles.containsKey(noble.getObjectId()))
		{
			int classId = noble.getBaseClassId();
			if(classId < 88) // Если это не 3-я профа, то исправляем со 2-й на 3-ю.
				for(ClassId id : ClassId.VALUES)
					if(id.level() == 3 && id.getParent(0).getId() == classId)
					{
						classId = id.getId();
						break;
					}

			StatsSet statDat = new StatsSet();
			statDat.set(CLASS_ID, classId);
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, Config.OLYMPIAD_POINTS_DEFAULT);
			statDat.set(POINTS_PAST, 0);
			statDat.set(POINTS_PAST_STATIC, 0);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WIN, 0);
			statDat.set(COMP_LOOSE, 0);
			statDat.set(GAME_CLASSES_COUNT, 0);
			statDat.set(GAME_NOCLASSES_COUNT, 0);
			statDat.set(GAME_TEAM_COUNT, 0);

			_nobles.put(noble.getObjectId(), statDat);
			OlympiadDatabase.saveNobleData();
		}
	}

	public static synchronized void removeNoble(Player noble)
	{
		_nobles.remove(noble.getObjectId());
		OlympiadDatabase.saveNobleData();
	}

	public static int getPeriod()
	{
		return _period;
	}

	public static int getCountOpponents()
	{
		return _nonClassBasedRegisters.size() + _classBasedRegisters.size() + _teamBasedRegisters.size();
	}
}