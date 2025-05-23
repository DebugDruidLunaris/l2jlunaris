package jts.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.database.DatabaseFactory;

public class AccountReportDAO
{
	private static final Logger _log = LoggerFactory.getLogger(AccountReportDAO.class);
	private static final AccountReportDAO _instance = new AccountReportDAO(null);
	
	public static final String SELECT_SQL_QUERY = "SELECT bot_report_points FROM accounts WHERE login = ?";
	public static final String UPDATE_SQL_QUERY = "UPDATE " + Config.LOGIN_DB + " accounts SET bot_report_points = ? WHERE login = ?";
	
	private int _reportBotPoints;
	
	public static AccountReportDAO getInstance()
	{
		return _instance;
	}

	public AccountReportDAO(String accName)
	{
		loadBotPoints(accName);
	}
	
	private void loadBotPoints(String accName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement =  con.prepareStatement(SELECT_SQL_QUERY);
			statement.setString(1, accName);
			
			rset = statement.executeQuery();
			while(rset.next())
			{
				_reportBotPoints = rset.getInt("bot_report_points");
			}
		}
		catch(Exception e)
		{
			_log.info("AccountReportDAO.loadBotPoints(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public synchronized void updatePoints(String accName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_SQL_QUERY);
			statement.setInt(1, _reportBotPoints);
			statement.setString(2, accName);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("AccountReportDAO.updatePoints(String): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public synchronized int getReportsPoints()
	{
		return _reportBotPoints;
	}
	
	public synchronized void reducePoints()
	{
		_reportBotPoints--;
	}
}