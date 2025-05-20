package jts.gameserver.network;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.Ex2ndPasswordAck;
import jts.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import jts.gameserver.network.serverpackets.Ex2ndPasswordVerify;
import jts.gameserver.utils.Base64;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Util;

public class SecondaryPasswordAuth
{
	private final Logger _log = Logger.getLogger(SecondaryPasswordAuth.class.getName());
	private final GameClient _activeClient;

	private String _password;
	private int _wrongAttempts;
	private boolean _authed;

	private static final String VAR_PWD = "secauth_pwd";
	private static final String VAR_WTE = "secauth_wte";

	private static final String SELECT_PASSWORD = "SELECT var, value FROM character_secondary_password WHERE account_name=? AND var LIKE 'secauth_%'";
	private static final String INSERT_PASSWORD = "INSERT INTO character_secondary_password VALUES (?, ?, ?)";
	private static final String UPDATE_PASSWORD = "UPDATE character_secondary_password SET value=? WHERE account_name=? AND var=?";
	private static final String INSERT_ATTEMPT = "INSERT INTO character_secondary_password VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?";

	/**
	 * @param gameClient 
	 */
	public SecondaryPasswordAuth(GameClient gameClient)
	{
		_activeClient = gameClient;
		_password = null;
		_wrongAttempts = 0;
		_authed = false;
		loadPassword();
	}

	private void loadPassword()
	{
		String var, value = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_PASSWORD);
			statement.setString(1, _activeClient.getLogin());
			rset = statement.executeQuery();
			while(rset.next())
			{
				var = rset.getString("var");
				value = rset.getString("value");

				if(var.equals(VAR_PWD))
					_password = value;
				else if(var.equals(VAR_WTE))
					_wrongAttempts = Integer.parseInt(value);
			}
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error while reading password.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean savePassword(String password)
	{
		if(passwordExist())
		{
			_log.warning("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced savePassword");
			_activeClient.closeNow(true);
			return false;
		}

		if(!validatePassword(password))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}

		password = cryptPassword(password);

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_PASSWORD);
			statement.setString(1, _activeClient.getLogin());
			statement.setString(2, VAR_PWD);
			statement.setString(3, password);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error while writing password.", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_password = password;
		return true;
	}

	public boolean insertWrongAttempt(int attempts)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_ATTEMPT);
			statement.setString(1, _activeClient.getLogin());
			statement.setString(2, VAR_WTE);
			statement.setString(3, Integer.toString(attempts));
			statement.setString(4, Integer.toString(attempts));
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error while writing wrong attempts.", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return true;
	}

	public boolean changePassword(String oldPassword, String newPassword)
	{
		if(!passwordExist())
		{
			_log.warning("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced changePassword");
			_activeClient.closeNow(true);
			return false;
		}

		if(!checkPassword(oldPassword, true))
			return false;

		if(!validatePassword(newPassword))
		{
			_activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}

		newPassword = cryptPassword(newPassword);

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(UPDATE_PASSWORD);
			statement.setString(1, newPassword);
			statement.setString(2, _activeClient.getLogin());
			statement.setString(3, VAR_PWD);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Error while reading password.", e);
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_password = newPassword;
		_authed = false;
		return true;
	}

	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);

		if(!password.equals(_password))
		{
			_wrongAttempts++;
			if(_wrongAttempts < Config.SECOND_AUTH_MAX_ATTEMPTS)
			{
				_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_WRONG, _wrongAttempts));
				insertWrongAttempt(_wrongAttempts);
				return false;
			}
			else
			{
				if(Config.SECOND_AUTH_BAN_ACC)
					banAccount(_activeClient.getLogin());
				Log.add(_activeClient.getLogin() + " - (" + _activeClient.getIpAddr() + ") has inputted the wrong password " + _wrongAttempts + " times in row.", "banned_accounts");
				insertWrongAttempt(0);
				_activeClient.close(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN, Config.SECOND_AUTH_MAX_ATTEMPTS));
				return false;
			}
		}
		if(!skipAuth)
		{
			_authed = true;
			_activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_OK, _wrongAttempts));
		}
		insertWrongAttempt(0);
		return true;
	}

	private void banAccount(String loginName)
	{
		int banTime = (int) (System.currentTimeMillis() / 1000L + Config.SECOND_AUTH_BAN_TIME * 60);

		LoginServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(_activeClient.getLogin(), 0, banTime));
		GameClient client = LoginServerCommunication.getInstance().getAuthedClient(_activeClient.getLogin());

		if(client != null)
		{
			Player player = client.getActiveChar();
			if(player != null)
				player.kick();
		}
	}

	public boolean passwordExist()
	{
		return _password == null ? false : true;
	}

	public void openDialog()
	{
		if(passwordExist())
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_PROMPT));
		else
			_activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_NEW));
	}

	public boolean isAuthed()
	{
		return _authed;
	}

	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.encodeBytes(hash);
		}
		catch(NoSuchAlgorithmException e)
		{
			_log.severe("[SecondaryPasswordAuth]Unsupported Algorythm");
		}
		catch(UnsupportedEncodingException e)
		{
			_log.severe("[SecondaryPasswordAuth]Unsupported Encoding");
		}
		return null;
	}

	private boolean validatePassword(String password)
	{
		if(!Util.isDigit(password))
			return false;

		if(password.length() < 6 || password.length() > 8)
			return false;

		if(Config.SECOND_AUTH_STRONG_PASS)
		{
			for(int i = 0; i < password.length() - 1; i++)
			{
				char curCh = password.charAt(i);
				char nxtCh = password.charAt(i + 1);

				if(curCh + 1 == nxtCh)
					return false;
				else if(curCh - 1 == nxtCh)
					return false;
				else if(curCh == nxtCh)
					return false;
			}
			for(int i = 0; i < password.length() - 2; i++)
			{
				String toChk = password.substring(i + 1);
				StringBuffer chkEr = new StringBuffer(password.substring(i, i + 2));

				if(toChk.contains(chkEr))
					return false;
				else if(toChk.contains(chkEr.reverse()))
					return false;
			}
		}
		_wrongAttempts = 0;
		return true;
	}
}