package jts.gameserver.loginservercon.gspackets;

import jts.gameserver.loginservercon.SendablePacket;

public class PlayerLogout extends SendablePacket
{
	private String account;

	public PlayerLogout(String account)
	{
		this.account = account;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x04);
		writeS(account);
	}
}
