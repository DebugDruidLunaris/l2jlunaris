package zones;

import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.ReflectionUtils;

public class BelethForbidden implements ScriptFile
{

    private static ZoneListener _zoneListener;

	@Override
	public void onLoad()
    {
        _zoneListener = new ZoneListener();
        Zone zone = ReflectionUtils.getZone("[20_24_water1]");
        zone.addListener(_zoneListener);
    }
	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}
    public class ZoneListener implements OnZoneEnterLeaveListener 
    {

        public ZoneListener() 
        {
        }

        public void onZoneEnter(Zone zone, Creature cha) 
        {
            if (!cha.isPlayable()) 
            {
                return;
            }
            Player player = cha.getPlayer();
            if (player != null)
            {
            	player.sendMessage(player.isLangRus() ? "Вы не можете находиться здесь :D ." : "You can not be here. :D ");
            	player.teleToLocation(18568, 145528, -3120);
                }
            }
        

        public void onZoneLeave(Zone zone, Creature cha) {
        }
    }
}
