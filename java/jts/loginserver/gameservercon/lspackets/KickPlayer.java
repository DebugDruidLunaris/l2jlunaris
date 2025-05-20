package jts.loginserver.gameservercon.lspackets;

import jts.loginserver.gameservercon.SendablePacket;

public class KickPlayer extends SendablePacket
{
	private String account;

	public KickPlayer(String login)
	{
		account = login;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x03);
		writeS(account);
	}
}