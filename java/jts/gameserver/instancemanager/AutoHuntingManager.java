package jts.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import jts.commons.dbutils.DbUtils;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.dao.AccountReportDAO;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.AutoHuntingPunish;


public class AutoHuntingManager
{
  private static final Logger _log = LoggerFactory.getLogger(AutoHuntingManager.class);
  private static AutoHuntingManager _instance;
  private static FastMap<Integer, String[]> _unread;
  @SuppressWarnings({ "unchecked", "rawtypes" })
private static FastMap<Integer, FastList<Player>> _reportedCount = new FastMap();

  @SuppressWarnings({ "unchecked", "rawtypes" })
private static FastMap<Integer, Long> _lockedReporters = new FastMap();

  @SuppressWarnings({ "rawtypes", "unchecked" })
private static Set<String> _lockedIps = new HashSet();

  @SuppressWarnings({ "rawtypes", "unchecked" })
private static Set<String> _lockedAccounts = new HashSet();

  private AutoHuntingManager()
  {
    loadUnread();
  }

  public static AutoHuntingManager getInstance()
  {
    if (_instance == null) {
      _instance = new AutoHuntingManager();
    }
    return _instance;
  }

  private static boolean reportedIsOnline(Player player)
  {
    return World.getPlayer(player.getObjectId()) != null;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
public synchronized void reportBot(Player reported, Player reporter)
  {
    if (!reportedIsOnline(reported))
    {
      reporter.sendMessage("The player you are reporting is offline.");
      return;
    }

    _lockedReporters.put(Integer.valueOf(reporter.getObjectId()), Long.valueOf(System.currentTimeMillis()));
    _lockedIps.add(reporter.getIP());
    _lockedAccounts.add(reporter.getAccountName());

    long date = Calendar.getInstance().getTimeInMillis();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      if (!_reportedCount.containsKey(reported))
      {
        FastList p = new FastList();
        p.add(reported);
        _reportedCount.put(Integer.valueOf(reporter.getObjectId()), p);
      }
      else
      {
        if (((FastList)_reportedCount.get(reporter)).contains(Integer.valueOf(reported.getObjectId()))) {
          reporter.sendMessage("You cannot report a player more than 1 time");
          return;
        }
        ((FastList)_reportedCount.get(reporter)).add(reported);
      }

      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO `bot_report`(`reported_name`, `reported_objectId`, `reporter_name`, `reporter_objectId`, `date`) VALUES (?,?,?,?,?)", 1);
      statement.setString(1, reported.getName());
      statement.setInt(2, reported.getObjectId());
      statement.setString(3, reporter.getName());
      statement.setInt(4, reporter.getObjectId());
      statement.setLong(5, date);
      statement.executeUpdate();

      rset = statement.getGeneratedKeys();
      rset.next();
      int maxId = rset.getInt(1);

      _unread.put(Integer.valueOf(maxId), new String[] { reported.getName(), reporter.getName(), String.valueOf(date) });
    }
    catch (Exception e)
    {
      _log.warn("Could not save reported bot " + reported.getName() + " by " + reporter.getName() + " at " + date + ".");
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    SystemMessage2 sm = new SystemMessage2(SystemMsg.C1_REPORTED_AS_BOT);
    sm.addName(reported);
    reporter.sendPacket(sm);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
private void loadUnread()
  {
    _unread = new FastMap();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `report_id`, `reported_name`, `reporter_name`, `date` FROM `bot_report` WHERE `read` = ?");
      statement.setString(1, "false");

      rset = statement.executeQuery();
      while (rset.next())
      {
        String[] data = new String[3];
        data[0] = rset.getString("reported_name");
        data[1] = rset.getString("reporter_name");
        data[2] = rset.getString("date");

        _unread.put(Integer.valueOf(rset.getInt("report_id")), data);
      }
    }
    catch (Exception e)
    {
      _log.warn("Could not load data from bot_report:\n" + e.getMessage());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public FastMap<Integer, String[]> getUnread()
  {
    return _unread;
  }

  public void markAsRead(int id)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `bot_report` SET `read` = ? WHERE `report_id` = ?");
      statement.setString(1, "true");
      statement.setInt(2, id);
      statement.execute();

      _unread.remove(Integer.valueOf(id));
      _log.info("Reported bot marked as read, id was: " + id);
    }
    catch (Exception e)
    {
      _log.warn("Could not mark as read the reported bot: " + id + ":\n" + e.getMessage());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public int getPlayerReportsCount(Player reported)
  {
    int count = 0;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT COUNT(*) FROM `bot_report` WHERE `reported_objectId` = ?");
      statement.setInt(1, reported.getObjectId());

      rset = statement.executeQuery();
      if (rset.next())
        count = rset.getInt(1);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    return count;
  }

  public void savePlayerPunish(Player punished)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `bot_reported_punish` SET `time_left` = ? WHERE `charId` = ?");
      statement.setLong(1, punished.getPlayerPunish().getPunishTimeLeft());
      statement.setInt(2, punished.getObjectId());
      statement.execute();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean validateBot(Player reported, Player reporter)
  {
    if ((reported == null) || (reporter == null)) {
      return false;
    }

    if ((reported.isInPeaceZone()) || (reported.isInCombatZone()) || (reported.isInOlympiadMode()))
    {
      reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT));
      return false;
    }

    if ((reported.getClan() != null) && (reporter.getClan() != null))
    {
      if (reported.getClan().isAtWarWith(reporter.getClanId()))
      {
        reporter.sendPacket(new SystemMessage2(SystemMsg.CANNOT_REPORT_TARGET_IN_CLAN_WAR));
        return false;
      }
    }

    if (!reported.hasEarnedExp())
    {
      reporter.sendPacket(new SystemMessage2(SystemMsg.CANNOT_REPORT_CHARACTER_WITHOUT_GAINEXP));
      return false;
    }

 		if (_reportedCount.containsKey(reporter))
 		{
 			for (Player p : _reportedCount.get(reporter))
 			{
 				if (reported == p)
 				{
 					reporter.sendPacket(new SystemMessage2(SystemMsg.C1_REPORTED_AS_BOT));
 					return false;
 				}
 			}
 		}
 		return true;
 	}

  public synchronized boolean validateReport(Player reporter)
  {
    if (reporter == null) {
      return false;
    }
    if (reporter._account == null) {
      reporter._account = new AccountReportDAO(reporter.getAccountName());
    }

    if (reporter._account.getReportsPoints() == 0)
    {
      SystemMessage sm = new SystemMessage(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
      sm.addNumber(0);
      sm.addNumber(0);
      reporter.sendPacket(sm);
      return false;
    }

    if (_lockedReporters.containsKey(Integer.valueOf(reporter.getObjectId())))
    {
      long delay = System.currentTimeMillis() - ((Long)_lockedReporters.get(Integer.valueOf(reporter.getObjectId()))).longValue();
      if (delay <= 1800000L)
      {
        int left = (int)(1800000L - delay) / 60000;
        SystemMessage sm = new SystemMessage(SystemMsg.YOU_CAN_REPORT_IN_S1_MINUTES_S2_REPORT_POINTS_REMAIN_IN_ACCOUNT);
        sm.addNumber(left);
        sm.addNumber(reporter._account.getReportsPoints());
        reporter.sendPacket(sm);
        return false;
      }
      
      ThreadPoolManager.getInstance().execute(new ReportClear(reporter));
    }
    else {
      if (_lockedIps.contains(reporter.getIP()))
      {
        reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
        return false;
      }

      if (_lockedAccounts.contains(reporter.getAccountName()))
      {
        reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_BECAUSE_ANOTHER_CHARACTER_FROM_THIS_ACCOUNT_HAS_ALREADY_DONE_SO));
        return false;
      }

      if (reporter.getClan() != null)
		{
			for (int i : _lockedReporters.keySet())
			{
				// Same clan
				Player p = World.getPlayer(i);
				if (p == null)
					continue;
				
				if (p.getClanId() == reporter.getClanId())
				{
					reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
					return false;
				}
				// Same ally
				else if (reporter.getClan().getAllyId() != 0)
				{
					if (p.getClan().getAllyId() == reporter.getClan().getAllyId())
					{
						reporter.sendPacket(new SystemMessage2(SystemMsg.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_));
						return false;
					}
				}
			}
		}
		reporter._account.reducePoints();
		return true;
	}
	return false;
  }

  public void onEnter(Player activeChar)
  {
    activeChar.setFirstExp(activeChar.getExp());
    restorePlayerBotPunishment(activeChar);
    activeChar._account = new AccountReportDAO(activeChar.getAccountName());
  }

  private void restorePlayerBotPunishment(Player activeChar)
  {
    String punish = "";
    long delay = 0L;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `punish_type`, `time_left` FROM `bot_reported_punish` WHERE `charId` = ?");
      statement.setInt(1, activeChar.getObjectId());

      rset = statement.executeQuery();
      while (rset.next())
      {
        punish = rset.getString("punish_type");
        delay = rset.getLong("time_left");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    if ((!punish.isEmpty()) && (AutoHuntingPunish.Punish.valueOf(punish) != null))
    {
      if (delay < 0L)
      {
        AutoHuntingPunish.Punish p = AutoHuntingPunish.Punish.valueOf(punish);
        long left = -delay / 1000L / 60L;
        activeChar.setPunishDueBotting(p, (int)left);
      }
      else {
        activeChar.endPunishment();
      }
    }
  }

  private class ReportClear
    implements Runnable
  {
    private Player _reporter;

    private ReportClear(Player reporter)
    {
      this._reporter = reporter;
    }

    public void run()
    {
      AutoHuntingManager._lockedReporters.remove(Integer.valueOf(this._reporter.getObjectId()));
      AutoHuntingManager._lockedIps.remove(this._reporter.getNetConnection());
      AutoHuntingManager._lockedAccounts.remove(this._reporter.getAccountName());
    }
  }
}