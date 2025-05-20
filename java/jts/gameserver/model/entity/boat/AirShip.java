package jts.gameserver.model.entity.boat;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExAirShipInfo;
import jts.gameserver.network.serverpackets.ExGetOffAirShip;
import jts.gameserver.network.serverpackets.ExGetOnAirShip;
import jts.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import jts.gameserver.network.serverpackets.ExMoveToLocationInAirShip;
import jts.gameserver.network.serverpackets.ExStopMoveAirShip;
import jts.gameserver.network.serverpackets.ExStopMoveInAirShip;
import jts.gameserver.network.serverpackets.ExValidateLocationInAirShip;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.templates.CharTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class AirShip extends Boat
{
	public AirShip(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public L2GameServerPacket infoPacket()
	{
		return new ExAirShipInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new ExMoveToLocationAirShip(this);
	}

	@Override
	public L2GameServerPacket inMovePacket(Player player, Location src, Location desc)
	{
		return new ExMoveToLocationInAirShip(player, this, src, desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new ExStopMoveAirShip(this);
	}

	@Override
	public L2GameServerPacket inStopMovePacket(Player player)
	{
		return new ExStopMoveInAirShip(player);
	}

	@Override
	public L2GameServerPacket startPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket checkLocationPacket()
	{
		return null;
	}

	@Override
	public L2GameServerPacket validateLocationPacket(Player player)
	{
		return new ExValidateLocationInAirShip(player);
	}

	@Override
	public L2GameServerPacket getOnPacket(Player player, Location location)
	{
		return new ExGetOnAirShip(player, this, location);
	}

	@Override
	public L2GameServerPacket getOffPacket(Player player, Location location)
	{
		return new ExGetOffAirShip(player, this, location);
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	@Override
	public void oustPlayers()
	{
		for(Player player : _players)
			oustPlayer(player, getReturnLoc(), true);
	}
}