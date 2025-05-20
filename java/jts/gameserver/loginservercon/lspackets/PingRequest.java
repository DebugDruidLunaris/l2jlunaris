package jts.gameserver.loginservercon.lspackets;

import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.loginservercon.gspackets.PingResponse;

public class PingRequest extends ReceivablePacket
{
	@Override
	public void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		LoginServerCommunication.getInstance().sendPacket(new PingResponse());
	}
}