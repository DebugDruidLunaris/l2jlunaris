package jts.gameserver.network.serverpackets;

import jts.gameserver.utils.Location;

public class ObserverStart extends L2GameServerPacket
{
	// ddSS
	private Location _loc;

	public ObserverStart(Location loc)
	{
		_loc = loc;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xeb);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeC(0x00);
		writeC(0xc0);
		writeC(0x00);
	}
}