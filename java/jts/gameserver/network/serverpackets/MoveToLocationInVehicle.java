package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.boat.Boat;
import jts.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _playerObjectId, _boatObjectId;
	private Location _origin, _destination;

	public MoveToLocationInVehicle(Player cha, Boat boat, Location origin, Location destination)
	{
		_playerObjectId = cha.getObjectId();
		_boatObjectId = boat.getObjectId();
		_origin = origin;
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x7e);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}