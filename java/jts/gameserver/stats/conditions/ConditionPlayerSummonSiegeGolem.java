package jts.gameserver.stats.conditions;

import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.stats.Env;

public class ConditionPlayerSummonSiegeGolem extends Condition
{
	public ConditionPlayerSummonSiegeGolem() {}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean testImpl(Env env)
	{
		Player player = env.character.getPlayer();
		if(player == null)
			return false;
		Zone zone = player.getZone(Zone.ZoneType.RESIDENCE);
		if(zone != null)
			return false;
		zone = player.getZone(Zone.ZoneType.SIEGE);
		if(zone == null)
			return false;
		SiegeEvent event = player.getEvent(SiegeEvent.class);
		if(event == null)
			return false;
		if(event instanceof CastleSiegeEvent)
		{
			if(zone.getParams().getInteger("residence") != event.getId())
				return false;
			if(event.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan()) == null)
				return false;
		}
		else if(event.getSiegeClan(SiegeEvent.DEFENDERS, player.getClan()) == null)
			return false;

		return true;
	}
}