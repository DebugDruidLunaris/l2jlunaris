package zones;

import java.util.List;

import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.entity.events.impl.PlayerVsPlayerDuelEvent;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.ReflectionUtils;

public class DuelZone implements ScriptFile
{
	private static ZoneListener _zoneListener;

	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		List<Zone> zones = ReflectionUtils.getZonesByType(ZoneType.peace_zone);
		for(Zone zone : zones)
			zone.addListener(_zoneListener);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			if( !cha.isPlayer())
				return;

			Player player = (Player) cha;
			if( !player.isInDuel())
				return;

			DuelEvent duelEvent = player.getEvent(DuelEvent.class);
			if(duelEvent != null && duelEvent instanceof PlayerVsPlayerDuelEvent)
				duelEvent.stopEvent();
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha) {}
	}
}