package jts.loginserver.gameservercon.lspackets;

import jts.loginserver.SessionKey;
import jts.loginserver.accounts.Account;
import jts.loginserver.accounts.SessionManager.Session;
import jts.loginserver.gameservercon.SendablePacket;

public class PlayerAuthResponse extends SendablePacket
{
	private String login;
	private boolean authed;
	private int playOkID1;
	private int playOkID2;
	private int loginOkID1;
	private int loginOkID2;
	private double bonus;
	private int bonusExpire;
	private String hwid;

	public PlayerAuthResponse(Session session, boolean authed)
	{
		Account account = session.getAccount();
		login = account.getLogin();
		this.authed = authed;
		if(authed)
		{
			SessionKey skey = session.getSessionKey();
			playOkID1 = skey.playOkID1;
			playOkID2 = skey.playOkID2;
			loginOkID1 = skey.loginOkID1;
			loginOkID2 = skey.loginOkID2;
			bonus = account.getBonus();
			bonusExpire = account.getBonusExpire();
		}
		hwid = account.getAllowedHwid();
	}

	public PlayerAuthResponse(String account)
	{
		login = account;
		authed = false;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x02);
		writeS(login);
		writeC(authed ? 1 : 0);
		if(authed)
		{
			writeD(playOkID1);
			writeD(playOkID2);
			writeD(loginOkID1);
			writeD(loginOkID2);
			writeF(bonus);
			writeD(bonusExpire);
		}
		writeS(hwid);
	}
}
