package jts.gameserver.listener.zone.impl;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class NoLandingZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new NoLandingZoneListener();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		Player player = actor.getPlayer();
		if(player != null)
			if(player.isFlying() && player.getMountNpcId() == 12621)
			{
				Residence residence = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence", 0));
				if(residence != null && player.getClan() != null && residence.getOwner() == player.getClan())
				{}
				else
				{
					player.stopMove();
					player.sendPacket(SystemMsg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN);
					player.setMount(0, 0, 0);
				}
			}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature cha) {}
}