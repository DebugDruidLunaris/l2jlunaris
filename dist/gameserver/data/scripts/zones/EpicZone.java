package zones;

import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.skills.EffectType;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.ReflectionUtils;

public class EpicZone implements ScriptFile
{
	private static ZoneListener _zoneListener;

	@Override
	public void onLoad()
	{
		_zoneListener = new ZoneListener();
		Zone zone = ReflectionUtils.getZone("[queen_ant_epic]");
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
			Player player = cha.getPlayer();
			
			if(zone.getParams() == null || player == null)
				return;
			
			if(player.getLevel() > zone.getParams().getInteger("levelLimit"))
			{
				player.sendMessage(new CustomMessage("scripts.zones.epic.banishMsg", player));
				player.teleToLocation(Location.parseLoc(zone.getParams().getString("tele")));
			}
			else if(player.getTransformation() != 0)
			{
				Effect transform = player.getEffectList().getEffectByType(EffectType.Transformation);
				if(transform == null)
					return;
				if(transform.getSkill().isBaseTransformation() || transform.getSkill().isSummonerTransformation() || transform.getSkill().isSummonerTransformation())
					return;
				player.sendMessage(player.isLangRus() ? "Здесь запрещено находиться в трансформации." : "It is forbidden to be in transformation.");
				player.setTransformation(0);
				player.getEffectList().stopEffects(EffectType.Transformation);
			}
		}
		

		@Override
		public void onZoneLeave(Zone zone, Creature cha) {}
	}
}