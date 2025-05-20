package jts.gameserver.xmrpcserver.XMLServices;

import com.google.gson.Gson;
import jts.commons.dbutils.DbUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


abstract class Base
{
	protected static final Logger log = LogManager.getLogger(Base.class);
	protected static final Logger logDonate = LogManager.getLogger("donate");

	protected final Gson jsonObject;
	protected Connection conn;
	protected PreparedStatement statement;
	protected ResultSet resultSet;
	protected DbUtils ut;

	protected Base()
	{
		jsonObject = new Gson();
	}

	protected void databaseClose(boolean closeResultSet)
	{
		if(closeResultSet)
		{
		}
		else
		{
		}
	}

	public <T> String json(T data)
	{
		return jsonObject.toJson(data);
	}
}
