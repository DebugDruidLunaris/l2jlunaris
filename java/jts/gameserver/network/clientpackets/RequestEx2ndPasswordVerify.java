package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;

public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
	private String _password;

	@Override
	protected void readImpl()
	{
		_password = readS();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.SECOND_AUTH_ENABLED)
			return;

		getClient().getSecondaryAuth().checkPassword(_password, false);
	}
}