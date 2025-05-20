package jts.gameserver.model.entity.boat;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.GetOffVehicle;
import jts.gameserver.network.serverpackets.GetOnVehicle;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MoveToLocationInVehicle;
import jts.gameserver.network.serverpackets.StopMove;
import jts.gameserver.network.serverpackets.StopMoveToLocationInVehicle;
import jts.gameserver.network.serverpackets.ValidateLocationInVehicle;
import jts.gameserver.network.serverpackets.VehicleCheckLocation;
import jts.gameserver.network.serverpackets.VehicleDeparture;
import jts.gameserver.network.serverpackets.VehicleInfo;
import jts.gameserver.network.serverpackets.VehicleStart;
import jts.gameserver.templates.CharTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class Vehicle extends Boat
{
	public Vehicle(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return new VehicleStart(this);
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ValidateLocationInVehicle(player);
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return new VehicleCheckLocation(this);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new VehicleInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new VehicleDeparture(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new MoveToLocationInVehicle(player, this, src, desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new StopMoveToLocationInVehicle(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new GetOnVehicle(player, this, location);
	}

	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new GetOffVehicle(player, this, location);
	}

	@Override
	public void oustPlayers() {}

	@Override
	public boolean isVehicle()
	{
		return true;
	}
}