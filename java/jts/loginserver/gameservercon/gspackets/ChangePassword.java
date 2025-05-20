package jts.loginserver.gameservercon.gspackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.commons.dbutils.DbUtils;
import jts.loginserver.Config;
import jts.loginserver.database.L2DatabaseFactory;
import jts.loginserver.gameservercon.ReceivablePacket;
import jts.loginserver.gameservercon.lspackets.ChangePasswordResponse;

public class ChangePassword extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(ChangePassword.class);
	private String accname;
	String oldPass;
	String newPass;
	
	@Override
	protected void readImpl()
	{
		accname = readS();
		oldPass = readS();
		newPass = readS();
	}
	
	@SuppressWarnings("static-access")
	@Override
	protected void runImpl()
	{
		String dbPassword = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			try
			{
				statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?");
				statement.setString(1, accname);
				rs = statement.executeQuery();
				if (rs.next())
				{
					dbPassword = rs.getString("password");
				}
			}
			catch (Exception e)
			{
				_log.warn("Can't recive old password for account " + accname + ", exciption :" + e);
			}
			finally
			{
				DbUtils.closeQuietly(statement, rs);
			}
			try
			{
				if (!Config.DEFAULT_CRYPT.compare(oldPass, dbPassword))
				{
					ChangePasswordResponse cp1;
					cp1 = new ChangePasswordResponse(accname, false);
					sendPacket(cp1);
				}
				else
				{
					statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?");
					statement.setString(1, Config.DEFAULT_CRYPT.encrypt(newPass));
					statement.setString(2, accname);
					int result = statement.executeUpdate();
					ChangePasswordResponse cp1;
					cp1 = new ChangePasswordResponse(accname, result != 0);
					sendPacket(cp1);
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}
}
