package jts.gameserver.loginservercon.gspackets;

import jts.gameserver.loginservercon.SendablePacket;

public class ChangePassword extends SendablePacket
{
	private final String account;
	private final String oldPass;
	private final String newPass;

	public ChangePassword(String account, String oldPass, String newPass)
	{
		this.account = account;
		this.oldPass = oldPass;
		this.newPass = newPass;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x08);
		writeS(account);
		writeS(oldPass);
		writeS(newPass);
	}
}
