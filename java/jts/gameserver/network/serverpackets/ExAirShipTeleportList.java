package jts.gameserver.network.serverpackets;

import java.util.Collections;
import java.util.List;

import jts.gameserver.model.entity.boat.ClanAirShip;
import jts.gameserver.model.entity.events.objects.BoatPoint;

public class ExAirShipTeleportList extends L2GameServerPacket
{
	private int _fuel;
	private List<BoatPoint> _airports = Collections.emptyList();

	public ExAirShipTeleportList(ClanAirShip ship)
	{
		_fuel = ship.getCurrentFuel();
		_airports = ship.getDock().getTeleportList();
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x9A);
		writeD(_fuel); // current fuel
		writeD(_airports.size());

		for(int i = 0; i < _airports.size(); i++)
		{
			BoatPoint point = _airports.get(i);
			writeD(i - 1); // AirportID
			writeD(point.getFuel()); // need fuel
			writeD(point.x); // Airport x
			writeD(point.y); // Airport y
			writeD(point.z); // Airport z
		}
	}
}