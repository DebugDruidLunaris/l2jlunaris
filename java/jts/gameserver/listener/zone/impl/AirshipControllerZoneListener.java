package jts.gameserver.listener.zone.impl;

import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.boat.ClanAirShip;
import jts.gameserver.model.instances.ClanAirShipControllerInstance;

public class AirshipControllerZoneListener implements OnZoneEnterLeaveListener
{
	private ClanAirShipControllerInstance _controllerInstance;

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(_controllerInstance == null && actor instanceof ClanAirShipControllerInstance)
			_controllerInstance = (ClanAirShipControllerInstance) actor;
		else if(actor.isClanAirShip())
			_controllerInstance.setDockedShip((ClanAirShip) actor);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		if(actor.isClanAirShip())
			_controllerInstance.setDockedShip(null);
	}
}