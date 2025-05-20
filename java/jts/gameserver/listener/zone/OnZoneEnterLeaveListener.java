package jts.gameserver.listener.zone;

import jts.commons.listener.Listener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Zone;

public interface OnZoneEnterLeaveListener extends Listener<Zone>
{
	public void onZoneEnter(Zone zone, Creature actor);
	public void onZoneLeave(Zone zone, Creature actor);
}