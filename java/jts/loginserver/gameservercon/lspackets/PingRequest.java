package jts.loginserver.gameservercon.lspackets;

import jts.loginserver.gameservercon.SendablePacket;

public class PingRequest extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xff);
	}
}