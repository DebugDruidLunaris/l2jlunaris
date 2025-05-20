package events.TreasuresOfTheHerald;

import instances.TreasuresOfTheHeraldInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import jts.commons.dbutils.DbUtils;
import jts.commons.lang.reference.HardReference;
import jts.commons.lang.reference.HardReferences;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.InstantZoneHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.InstantZone;
import jts.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreasuresOfTheHerald extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(TreasuresOfTheHerald.class);

	public static final Location TEAM1_LOC = new Location(139736, 145832, -15264); // Team location after teleportation
	public static final Location TEAM2_LOC = new Location(139736, 139832, -15264);
	public static final Location RETURN_LOC = new Location(43816, -48232, -822);
	public static final int[] everydayStartTime = { 21, 30, 00 }; // hh mm ss

	private static boolean _active = false;
	private static boolean _isRegistrationActive = false;

	private static int _minLevel = Config.EVENT_TREASURES_OF_THE_HERALD_MIN_LEVEL;
	private static int _maxLevel = Config.EVENT_TREASURES_OF_THE_HERALD_MAX_LEVEL;
	private static int _groupsLimit = Config.EVENT_TREASURES_OF_THE_HERALD_MAX_GROUP; // Limit of groups can register
	private static int _minPartyMembers = Config.EVENT_TREASURES_OF_THE_HERALD_MINIMUM_PARTY_MEMBER; // self-explanatory
	private static long regActiveTime = 10 * 60 * 1000L; // Timelimit for registration	

	private static ScheduledFuture<?> _globalTask;
	private static ScheduledFuture<?> _regTask;
	private static ScheduledFuture<?> _countdownTask1;
	private static ScheduledFuture<?> _countdownTask2;
	private static ScheduledFuture<?> _countdownTask3;

	private static List<HardReference<Player>> leaderList = new CopyOnWriteArrayList<HardReference<Player>>();

	public static class RegTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			prepare();
		}
	}

	public static class Countdown extends RunnableImpl
	{
		int _timer;

		public Countdown(int timer)
		{
			_timer = timer;
		}

		@Override
		public void runImpl() throws Exception
		{
			Announcements.getInstance().announceToAll("До конца приема заявок на Сокровища Вестника осталось " + Integer.toString(_timer) + " Минут.");
		}
	}

	@Override
	public void onLoad()
	{
		if(Config.EVENT_TREASURES_OF_THE_HERALD_ENABLE)
		{
			_log.info("Carregado Evento: Tesouros do Herald");
			initTimer();
		}
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	private static void initTimer()
	{
		long day = 24 * 60 * 60 * 1000L;
		Calendar ci = Calendar.getInstance();
		ci.set(Calendar.HOUR_OF_DAY, everydayStartTime[0]);
		ci.set(Calendar.MINUTE, everydayStartTime[1]);
		ci.set(Calendar.SECOND, everydayStartTime[2]);

		long delay = ci.getTimeInMillis() - System.currentTimeMillis();
		if(delay < 0)
			delay = delay + day;

		if(_globalTask != null)
			_globalTask.cancel(true);
		_globalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Launch(), delay, day);
	}

	public static class Launch extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			activateEvent();
		}
	}

	private static boolean canBeStarted()
	{
		for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress())
				return false;
		return true;
	}

	private static boolean isActive()
	{
		return _active;
	}

	public static void activateEvent()
	{
		if(!isActive() && canBeStarted())
		{
			_regTask = ThreadPoolManager.getInstance().schedule(new RegTask(), regActiveTime);
			if(regActiveTime > 2 * 60 * 1000L) //display countdown announcements only when timelimit for registration is more than 3 mins
			{
				if(regActiveTime > 5 * 60 * 1000L)
					_countdownTask3 = ThreadPoolManager.getInstance().schedule(new Countdown(5), regActiveTime - 300 * 1000);

				_countdownTask1 = ThreadPoolManager.getInstance().schedule(new Countdown(2), regActiveTime - 120 * 1000);
				_countdownTask2 = ThreadPoolManager.getInstance().schedule(new Countdown(1), regActiveTime - 60 * 1000);
			}
			ServerVariables.set("Tesouros do Herald", "on");
			_log.info("Event 'Torneio Tesouros do Herald ativado.");
			Announcements.getInstance().announceToAll("Открыта регистрация для Ивента - Сокровища Вестника! Зарегистрируйтесь используя Community Board(Alt+B) -> Ивенты -> Сокровища Вестника! (Регистрация и Описания Ивента)");
			Announcements.getInstance().announceToAll("Заявки будут приниматься в течении " + regActiveTime / 60000 + "минут");
			_active = true;
			_isRegistrationActive = true;
		}
	}

	/**
	 * Cancels the event during registration time
	 */
	public static void deactivateEvent()
	{
		if(isActive())
		{
			stopTimers();
			ServerVariables.unset("TreasuresOfTheHerald");
			_log.info("Torneio Tesouros do Herald cancelado.");
			Announcements.getInstance().announceToAll("Турнир Сокровища Вестника отменен");
			_active = false;
			_isRegistrationActive = false;
			leaderList.clear();
		}
	}

	/**
	 * Shows groups and their leaders who's currently in registration list
	 */
	public void showStats()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			player.sendMessage("Сокровища Вестника Ивент не запущен");
			return;
		}

		StringBuilder string = new StringBuilder();
		String refresh = "<button value=\"Refresh\" action=\"bypass -h scripts_events.TreasuresOfTheHerald.TreasuresOfTheHerald:showStats\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		String start = "<button value=\"Start Now\" action=\"bypass -h scripts_events.TreasuresOfTheHerald.TreasuresOfTheHerald:startNow\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		int i = 0;

		if(!leaderList.isEmpty())
		{
			for(Player leader : HardReferences.unwrap(leaderList))
			{
				if(!leader.isInParty())
					continue;
				string.append("*").append(leader.getName()).append("*").append(" | group members: ").append(leader.getParty().getMemberCount()).append("\n\n");
				i++;
			}
			show("Есть" + i + " Лидеры групп, которые подписались на Ивент:\n\n" + string + "\n\n" + refresh + "\n\n" + start, player, null);
		}
		else
			show("Нет участников в данный момент\n\n" + refresh, player, null);
	}

	public void startNow()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive() || !canBeStarted())
		{
			player.sendMessage("Ивент Сокровища Вестника не был запущен");
			return;
		}

		prepare();
	}

	/**
	 * Handles the group applications and apply restrictions
	 */
	public void addGroup()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!_isRegistrationActive)
		{
			player.sendMessage("Регистрация на Турнир - Сокровища Вестника Подтвердили");
			return;
		}

		if(leaderList.contains(player.getRef()))
		{
			player.sendMessage("Вы уже зарегистрировались на турнир - Сокровища Вестника.");
			return;
		}

		if(!player.isInParty())
		{
			player.sendMessage("Вы не член группы и не может зарегистрироваться");
			return;
		}

		if(!player.getParty().isLeader(player))
		{
			player.sendMessage("Только лидер группы может зарегистрироваться");
			return;
		}
		if(player.getParty().isInCommandChannel())
		{
			player.sendMessage("Для участия в турнире вы должны покинуть командный канал");
			return;
		}

		if(leaderList.size() >= _groupsLimit)
		{
			player.sendMessage("Максимальное количество групп для участия в турнире была достигнута. Заявка отклонена");
			return;
		}

		List<Player> party = player.getParty().getPartyMembers();

		String[] abuseReason = 
		{
			"Не в игре",
			"Не в группа",
			"Эта группа неполная. Минимальное число участников группы - 6.",
			"Вы не является лидером группы, чтобы применить",
			"Вы не можете принять участия в турнире, Так как ваш уровень мал",
			"Вы в бою, Вы не можете принять участия в турнире",
			"Вы в дуэли, Вы не можете принять участия в турнире",
			"Вы зарегистрированы на другой Ивент, Вы не можете принять участия в турнире",
			"Вы Находится в списке ожидания Олимпиады или участвуя в ней",
			"Вы не в состоянии телепортироваться, несоблюдения требований турнира",
			"Вы находитесь в Dimensional Rift, несоблюдения требований турнира",
			"Zarich и Akhamanah Не Могут принимать участия в турнире",
			"Вы не находитесь в мирной зоне",
			"Нарушения правил регистрации", 
		};

		for(Player eachmember : party)
		{
			int abuseId = checkPlayer(eachmember, false);
			if(abuseId != 0)
			{
				player.sendMessage("Игрок " + eachmember.getName() + " " + abuseReason[abuseId - 1]);
				return;
			}
		}

		leaderList.add(player.getRef());
		player.getParty().broadcastMessageToPartyMembers("Ваша группа была включена в список ожидания. Пожалуйста, не регистрировать на другое мероприятие и не участвовать в дуэлях до начала турнира. Полный список требований турнира в Community Board (Alt+B)");
	}

	private static void stopTimers()
	{
		if(_regTask != null)
		{
			_regTask.cancel(false);
			_regTask = null;
		}
		if(_countdownTask1 != null)
		{
			_countdownTask1.cancel(false);
			_countdownTask1 = null;
		}
		if(_countdownTask2 != null)
		{
			_countdownTask2.cancel(false);
			_countdownTask2 = null;
		}
		if(_countdownTask3 != null)
		{
			_countdownTask3.cancel(false);
			_countdownTask3 = null;
		}
	}

	private static void prepare()
	{
		checkPlayers();
		shuffleGroups();

		if(isActive())
		{
			stopTimers();
			ServerVariables.unset("TreasuresOfTheHerald");
			_active = false;
			_isRegistrationActive = false;
		}

		if(leaderList.size() < 2)
		{
			leaderList.clear();
			Announcements.getInstance().announceToAll("турнир: Сокровища Вестника отменен из-за отсутствия участников");
			return;
		}

		Announcements.getInstance().announceToAll("Сокровища Вестника: Время Регистрации Окончено. Телепортация На турнир.");
		start();
	}

	/**
	 * @param player
	 * @param doCheckLeadership
	 * @return
	 * Handles all limits for every group member. Called 2 times: when registering group and before sending it to the instance
	 */
	private static int checkPlayer(Player player, boolean doCheckLeadership)
	{
		if(!player.isOnline())
			return 1;

		if(!player.isInParty())
			return 2;

		if(doCheckLeadership && (player.getParty() == null || !player.getParty().isLeader(player)))
			return 4;

		if(player.getParty() == null || player.getParty().getMemberCount() < _minPartyMembers)
			return 3;

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
			return 5;

		if(player.isMounted())
			return 6;

		if(player.isInDuel())
			return 7;

		if(player.getTeam() != TeamType.NONE)
			return 8;

		if(player.getOlympiadGame() != null || Olympiad.isRegistered(player))
			return 9;

		if(player.isTeleporting())
			return 10;

		if(player.getParty().isInDimensionalRift())
			return 11;

		if(player.isCursedWeaponEquipped())
			return 12;

		if(!player.isInPeaceZone())
			return 13;

		if(player.isInObserverMode())
			return 14;

		return 0;
	}

	/**
	 * @return
	 * Grupos Misturados e separados em duas listas de igual tamanho
	 */
	private static void shuffleGroups()
	{
		if(leaderList.size() % 2 != 0) // Se não houver quantidade ímpar de grupos na lista, devemos remover um deles para torná-lo ainda
		{
			int rndindex = Rnd.get(leaderList.size());
			Player expelled = leaderList.remove(rndindex).get();
			if(expelled != null)
				expelled.sendMessage("При формировании списка участников турнира ваша команда была снята. Приносим свои извинения, попробуйте в следующий раунд.");
		}

		//Перемешиваем список
		for(int i = 0; i < leaderList.size(); i++)
		{
			int rndindex = Rnd.get(leaderList.size());
			leaderList.set(i, leaderList.set(rndindex, leaderList.get(i)));
		}
	}

	private static void checkPlayers()
	{
		for(Player player : HardReferences.unwrap(leaderList))
		{
			if(checkPlayer(player, true) != 0)
			{
				leaderList.remove(player.getRef());
				continue;
			}

			for(Player partymember : player.getParty().getPartyMembers())
				if(checkPlayer(partymember, false) != 0)
				{
					player.sendMessage("Ваша команда была дисквалифицирован и удалена с турнира из-за одного или нескольких членов группы, которые нарушили условия участия");
					leaderList.remove(player.getRef());
					break;
				}
		}
	}

	public static void updateWinner(Player winner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO event_data(charId, score) VALUES (?,1) ON DUPLICATE KEY UPDATE score=score+1");
			statement.setInt(1, winner.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private static void start()
	{
		int instancedZoneId = 504;
		InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
		if(iz == null)
		{
			_log.warn("Сокровища Вестника: Временная зона: " + instancedZoneId + " не найдена!");
			return;
		}

		for(int i = 0; i < leaderList.size(); i += 2)
		{
			Player team1Leader = leaderList.get(i).get();
			Player team2Leader = leaderList.get(i + 1).get();

			TreasuresOfTheHeraldInstance r = new TreasuresOfTheHeraldInstance();
			r.setTeam1(team1Leader.getParty());
			r.setTeam2(team2Leader.getParty());
			r.init(iz);
			r.setReturnLoc(TreasuresOfTheHerald.RETURN_LOC);

			for(Player member : team1Leader.getParty().getPartyMembers())
			{
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();

				member.teleToLocation(Location.findPointToStay(TreasuresOfTheHerald.TEAM1_LOC, 0, 150, r.getGeoIndex()), r);
			}

			for(Player member : team2Leader.getParty().getPartyMembers())
			{
				Functions.unRide(member);
				Functions.unSummonPet(member, true);
				member.setTransformation(0);
				member.setInstanceReuse(instancedZoneId, System.currentTimeMillis());
				member.dispelBuffs();

				member.teleToLocation(Location.findPointToStay(TreasuresOfTheHerald.TEAM2_LOC, 0, 150, r.getGeoIndex()), r);
			}

			r.start();
		}

		leaderList.clear();
		_log.info("Сокровища Вестника: Турнир запустился успешно.");
	}
}