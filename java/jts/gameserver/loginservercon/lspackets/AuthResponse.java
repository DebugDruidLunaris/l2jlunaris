package jts.gameserver.loginservercon.lspackets;

import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.loginservercon.gspackets.OnlineStatus;
import jts.gameserver.loginservercon.gspackets.PlayerInGame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthResponse extends ReceivablePacket
{
	private static final Logger _log = LoggerFactory.getLogger(AuthResponse.class);

	private int _serverId;
	private String _serverName;

	@Override
	protected void readImpl()
	{
		_serverId = readC();
		_serverName = readS();
	}

	@Override
	protected void runImpl()
	{
		_log.info("Registered on loginserver as " + _serverId + " [" + _serverName + "]");
		_log.info("|===== Server has successfully launched =====|");


		sendPacket(new OnlineStatus(true));

		String[] accounts = LoginServerCommunication.getInstance().getAccounts();
		for(String account : accounts)
			sendPacket(new PlayerInGame(account));
	}
}
