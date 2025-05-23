package jts.gameserver.loginservercon.lspackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.loginservercon.gspackets.SetAccountInfo;

import org.napile.primitive.Containers;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.impl.ArrayIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 21:05/25.03.2011
 */
public class GetAccountInfo extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(GetAccountInfo.class);
	private String _account;

	@Override
	protected void readImpl()
	{
		_account = readS();
	}

	@Override
	protected void runImpl()
	{
		int playerSize = 0;
		IntList deleteChars = Containers.EMPTY_INT_LIST;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?");
			statement.setString(1, _account);
			rset = statement.executeQuery();
			while(rset.next())
			{
				playerSize++;
				int d = rset.getInt("deletetime");
				if(d > 0)
				{
					if(deleteChars.isEmpty())
						deleteChars = new ArrayIntList(3);

					deleteChars.add(d + Config.DELETE_DAYS * 24 * 60 * 60);
				}
			}
		}
		catch(Exception e)
		{
			_log.error("GetAccountInfo:runImpl():" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		LoginServerCommunication.getInstance().sendPacket(new SetAccountInfo(_account, playerSize, deleteChars.toArray()));
	}
}
