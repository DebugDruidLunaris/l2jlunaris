package jts.loginserver.clientpackets;

import jts.loginserver.L2LoginClient;
import jts.loginserver.SessionKey;
import jts.loginserver.serverpackets.LoginFail.LoginFailReason;
import jts.loginserver.serverpackets.ServerList;
import jts.loginserver.serverpackets.ServerListFake;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	private boolean _loginFake = false;

	public RequestServerList(boolean login) 
	{
		_loginFake = login;
	}

	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
	}

	@Override
	protected void runImpl()
	{
		L2LoginClient client = getClient();
		if(_loginFake)
		{
			client.sendPacket(new ServerListFake(client.getAccount()));
			return;
		}
		SessionKey skey = client.getSessionKey();
		
		if(skey == null || !skey.checkLoginPair(_loginOkID1, _loginOkID2))
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		client.sendPacket(new ServerList(client.getAccount()));
	}
}