package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;
import jts.gameserver.utils.Location;

public class Ride extends L2GameServerPacket
{
	private int _mountType, _id, _rideClassID;
	private Location _loc;

	public Ride(Player cha)
	{
		_id = cha.getObjectId();
		_mountType = cha.getMountType();
		_rideClassID = cha.getMountNpcId() + 1000000;
		_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x8c);
		writeD(_id);
		writeD(_mountType == 0 ? 0 : 1);
		writeD(_mountType);
		writeD(_rideClassID);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}