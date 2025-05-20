package jts.gameserver.loginservercon.gspackets;

import jts.gameserver.loginservercon.SendablePacket;

public class PingResponse extends SendablePacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xff);
		writeQ(System.currentTimeMillis());
	}
}