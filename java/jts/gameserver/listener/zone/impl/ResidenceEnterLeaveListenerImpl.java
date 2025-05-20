package jts.gameserver.listener.zone.impl;

import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.entity.residence.ResidenceFunction;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.FuncMul;

public class ResidenceEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new ResidenceEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Player player = (Player) actor;
		Residence residence = (Residence) zone.getParams().get("residence");

		if(residence.getOwner() == null || residence.getOwner() != player.getClan())
			return;

		if(residence.isFunctionActive(ResidenceFunction.RESTORE_HP))
		{
			double value = 1. + residence.getFunction(ResidenceFunction.RESTORE_HP).getLevel() / 100.;

			player.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 0x30, residence, value));
		}

		if(residence.isFunctionActive(ResidenceFunction.RESTORE_MP))
		{
			double value = 1. + residence.getFunction(ResidenceFunction.RESTORE_MP).getLevel() / 100.;

			player.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 0x30, residence, value));
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Residence residence = (Residence) zone.getParams().get("residence");

		actor.removeStatsOwner(residence);
	}
}