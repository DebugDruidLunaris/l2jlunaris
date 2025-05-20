package npc.model;

import instances.ZakenDay;
import instances.ZakenDay83;
import instances.ZakenNight;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.ReflectionUtils;

@SuppressWarnings("serial")
public final class ZakenGatekeeperInstance extends NpcInstance
{
	private static final int nightZakenIzId = 114;
	private static final int dayZakenIzId = 133;
	private static final int ultraZakenIzId = 135;

	public ZakenGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_nightzaken"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(nightZakenIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(nightZakenIzId))
			{
				ReflectionUtils.enterReflection(player, new ZakenNight(), nightZakenIzId);
			}
		}
		else if(command.equalsIgnoreCase("request_dayzaken"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(dayZakenIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(dayZakenIzId))
			{
				ReflectionUtils.enterReflection(player, new ZakenDay(), dayZakenIzId);
			}
		}
		else if(command.equalsIgnoreCase("request_ultrazaken"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(ultraZakenIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(ultraZakenIzId))
			{
				ReflectionUtils.enterReflection(player, new ZakenDay83(), ultraZakenIzId);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}