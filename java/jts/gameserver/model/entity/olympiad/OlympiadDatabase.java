package jts.gameserver.model.entity.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.dao.OlympiadNobleDAO;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.instancemanager.ServerVariables;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.StatsSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadDatabase
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadDatabase.class);

	public static synchronized void loadNoblesRank()
	{
		Olympiad._noblesRank = new ConcurrentHashMap<Integer, Integer>();
		Map<Integer, Integer> tmpPlace = new HashMap<Integer, Integer>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OlympiadNobleDAO.GET_ALL_CLASSIFIED_NOBLESS);
			rset = statement.executeQuery();
			int place = 1;
			while(rset.next())
				tmpPlace.put(rset.getInt(Olympiad.CHAR_ID), place++);

		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Error!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);

		if(rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}

		for(int charId : tmpPlace.keySet())
			if(tmpPlace.get(charId) <= rank1)
				Olympiad._noblesRank.put(charId, 1);
			else if(tmpPlace.get(charId) <= rank2)
				Olympiad._noblesRank.put(charId, 2);
			else if(tmpPlace.get(charId) <= rank3)
				Olympiad._noblesRank.put(charId, 3);
			else if(tmpPlace.get(charId) <= rank4)
				Olympiad._noblesRank.put(charId, 4);
			else
				Olympiad._noblesRank.put(charId, 5);
	}

	/**
	 * Сбрасывает информацию о ноблесах, сохраняя очки за предыдущий период
	 */
	public static synchronized void cleanupNobles()
	{
		_log.info("Olympiad: Calculating last period...");
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(OlympiadNobleDAO.OLYMPIAD_CALCULATE_LAST_PERIOD);
			statement.setInt(1, Config.OLYMPIAD_BATTLES_FOR_REWARD);
			statement.execute();
			DbUtils.close(statement);

			statement = con.prepareStatement(OlympiadNobleDAO.OLYMPIAD_CLEANUP_NOBLES);
			statement.setInt(1, Config.OLYMPIAD_POINTS_DEFAULT);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldn't calculate last period!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		for(Integer nobleId : Olympiad._nobles.keySet())
		{
			StatsSet nobleInfo = Olympiad._nobles.get(nobleId);
			int points = nobleInfo.getInteger(Olympiad.POINTS);
			int compDone = nobleInfo.getInteger(Olympiad.COMP_DONE);
			nobleInfo.set(Olympiad.POINTS, Config.OLYMPIAD_POINTS_DEFAULT);
			if(compDone >= Config.OLYMPIAD_BATTLES_FOR_REWARD)
			{
				nobleInfo.set(Olympiad.POINTS_PAST, points);
				nobleInfo.set(Olympiad.POINTS_PAST_STATIC, points);
			}
			else
			{
				nobleInfo.set(Olympiad.POINTS_PAST, 0);
				nobleInfo.set(Olympiad.POINTS_PAST_STATIC, 0);
			}
			nobleInfo.set(Olympiad.COMP_DONE, 0);
			nobleInfo.set(Olympiad.COMP_WIN, 0);
			nobleInfo.set(Olympiad.COMP_LOOSE, 0);
			nobleInfo.set(Olympiad.GAME_CLASSES_COUNT, 0);
			nobleInfo.set(Olympiad.GAME_NOCLASSES_COUNT, 0);
			nobleInfo.set(Olympiad.GAME_TEAM_COUNT, 0);
		}
	}

	public static List<String> getClassLeaderBoard(int classId)
	{
		List<String> names = new ArrayList<String>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(classId == 132 ? OlympiadNobleDAO.GET_EACH_CLASS_LEADER_SOULHOUND : OlympiadNobleDAO.GET_EACH_CLASS_LEADER);
			statement.setInt(1, classId);
			rset = statement.executeQuery();
			while(rset.next())
				names.add(rset.getString(Olympiad.CHAR_NAME));
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldnt get heros from db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return names;
	}

	public static synchronized void sortHerosToBe()
	{
		if(Olympiad._period != 1)
			return;

		Olympiad._heroesToBe = new ArrayList<StatsSet>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			StatsSet hero;

			for(ClassId id : ClassId.VALUES)
			{
				if(id.getId() == 133)
					continue;
				if(id.level() == 3)
				{
					statement = con.prepareStatement(id.getId() == 132 ? OlympiadNobleDAO.OLYMPIAD_GET_HEROS_SOULHOUND : OlympiadNobleDAO.OLYMPIAD_GET_HEROS);
					statement.setInt(1, id.getId());
					statement.setInt(2, Config.OLYMPIAD_BATTLES_FOR_REWARD);
					rset = statement.executeQuery();

					if(rset.next())
					{
						hero = new StatsSet();
						hero.set(Olympiad.CLASS_ID, id.getId());
						hero.set(Olympiad.CHAR_ID, rset.getInt(Olympiad.CHAR_ID));
						hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));

						Olympiad._heroesToBe.add(hero);
					}
					DbUtils.close(statement, rset);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("Olympiad System: Couldnt heros from db!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static synchronized void saveNobleData(int nobleId)
	{
		OlympiadNobleDAO.getInstance().replace(nobleId);
	}

	public static synchronized void saveNobleData()
	{
		if(Olympiad._nobles == null)
			return;
		for(Integer nobleId : Olympiad._nobles.keySet())
			saveNobleData(nobleId);
	}

	public static synchronized void setNewOlympiadEnd()
	{
		Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(Olympiad._currentCycle));

		Calendar currentTime = Calendar.getInstance();
		if(!Config.OLYMPIAD_USE_MONTHLY_PERIOD) // По неделям.
		{
			currentTime.set(Calendar.DAY_OF_WEEK, Config.OLYMPIAD_WEEKLY_PERIOD_ENDDAY);
			currentTime.add(Calendar.WEEK_OF_YEAR, Config.OLYMPIAD_WEEKLY_WEEKCOUNT);
		}
		else if(Config.OLYMPIAD_PERIOD_END_DAYS.isEmpty() || (Config.OLYMPIAD_PERIOD_END_DAYS.size() == 1 && Config.OLYMPIAD_PERIOD_END_DAYS.get(0) == 1))
		{
			currentTime.add(Calendar.MONTH, 1);
			currentTime.set(Calendar.DAY_OF_MONTH, 1);
		}
		else
		{
			int nextDay = 0;
			int currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
			for(int day : Config.OLYMPIAD_PERIOD_END_DAYS)
			{
				if(currentDay < day)
				{
					nextDay = day;
					break;
				}
			}
			if(nextDay == 0)
			{
				nextDay = Config.OLYMPIAD_PERIOD_END_DAYS.get(0); //first day
				currentTime.add(Calendar.MONTH, 1);
				currentTime.set(Calendar.DAY_OF_MONTH, nextDay);
			}
			else
				currentTime.set(Calendar.DAY_OF_MONTH, nextDay);
		}
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 00);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		Olympiad._olympiadEnd = currentTime.getTimeInMillis();

		Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.OLYMPIAD_WPERIOD;


		Olympiad._isOlympiadEnd = false;
	}

	public static void save()
	{
		saveNobleData();
		ServerVariables.set("Olympiad_CurrentCycle", Olympiad._currentCycle);
		ServerVariables.set("Olympiad_Period", Olympiad._period);
		ServerVariables.set("Olympiad_End", Olympiad._olympiadEnd);
		ServerVariables.set("Olympiad_ValdationEnd", Olympiad._validationEnd);
		ServerVariables.set("Olympiad_NextWeeklyChange", Olympiad._nextWeeklyChange);
	}
}