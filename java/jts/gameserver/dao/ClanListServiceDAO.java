package jts.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.ClanListObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClanListServiceDAO
{
	private static final Logger _log = LoggerFactory.getLogger(ClanListServiceDAO.class);
	private static final ClanListServiceDAO _instance = new ClanListServiceDAO();

	public static final String SELECT_SQL_QUERY = "SELECT * FROM clanlist_service WHERE list_status=?";
	public static final String DELETE_SQL_QUERY = "DELETE FROM clanlist_service WHERE obj_id=?";
	public static final String CLEAR_SQL_QUERY = "DELETE FROM clanlist_service WHERE list_status=?";
	public static final String INSERT_SQL_QUERY = "INSERT INTO clanlist_service(obj_id, list_status, item_id, price) VALUES (?,?,?,?)";

	public static ClanListServiceDAO getInstance()
	{
		return _instance;
	}

	public List<ClanListObject> getData(int list_status)
	{
		List<ClanListObject> list = new ArrayList<ClanListObject>();
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, list_status);
			rset = statement.executeQuery();
			
			while(rset.next())
			{
				ClanListObject object = new ClanListObject(rset.getInt("obj_id"), list_status, rset.getInt("item_id"), rset.getInt("price"));
				list.add(object);
			}
		}
		catch(Exception e)
		{
			_log.info("ClanListServiceDAO.getData: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return list;
	}

	public void deleteFromList(int obj_id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, obj_id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("ClanListServiceDAO.deleteFromList(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void insertToList(int obj_id, int list_status, int item_id, int price)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, obj_id);
			statement.setInt(2, list_status);
			statement.setInt(3, item_id);
			statement.setInt(4, price);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("ClanListServiceDAO.insertToList(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public void clearList(int list_status)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CLEAR_SQL_QUERY);
			statement.setInt(1, list_status);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.info("ClanListServiceDAO.clearList(int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
