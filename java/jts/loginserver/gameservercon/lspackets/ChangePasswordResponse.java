package jts.loginserver.gameservercon.lspackets;

import jts.loginserver.gameservercon.SendablePacket;

public class ChangePasswordResponse extends SendablePacket
{
	private final String _account;
	boolean _hasChanged;

	public ChangePasswordResponse(String account, boolean hasChanged)
	{
		_account = account;
		_hasChanged = hasChanged;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x06);
		writeS(_account);
		writeD(_hasChanged ? 1 : 0);
	}
}
