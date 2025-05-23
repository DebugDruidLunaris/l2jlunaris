package jts.gameserver.network.serverpackets;

import jts.gameserver.utils.Location;

public class ObserverEnd extends L2GameServerPacket
{
	// ddSS
	private Location _loc;

	public ObserverEnd(Location loc)
	{
		_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xec);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}