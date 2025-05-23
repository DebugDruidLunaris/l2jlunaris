package jts.gameserver.utils;

import jts.commons.util.Rnd;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.instancemanager.MapRegionManager;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.RestartType;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.templates.mapregion.RestartArea;
import jts.gameserver.templates.mapregion.RestartPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleportUtils
{
	private static final Logger _log = LoggerFactory.getLogger(TeleportUtils.class);

	public final static Location DEFAULT_RESTART = new Location(17817, 170079, -3530);

	private TeleportUtils()
	{}

	public static Location getRestartLocation(Player player, RestartType restartType)
	{
		return getRestartLocation(player, player.getLoc(), restartType);
	}

	public static Location getRestartLocation(Player player, Location from, RestartType restartType)
	{
		Reflection r = player.getReflection();
		if(r != ReflectionManager.DEFAULT)
			if(r.getCoreLoc() != null)
				return r.getCoreLoc();
			else if(r.getReturnLoc() != null)
				return r.getReturnLoc();

		Clan clan = player.getClan();

		if(clan != null)
		{
			// If teleport to clan hall
			if(restartType == RestartType.TO_CLANHALL && clan.getHasHideout() != 0)
				return ResidenceHolder.getInstance().getResidence(clan.getHasHideout()).getOwnerRestartPoint();

			// If teleport to castle
			if(restartType == RestartType.TO_CASTLE && clan.getCastle() != 0)
				return ResidenceHolder.getInstance().getResidence(clan.getCastle()).getOwnerRestartPoint();

			// If teleport to fortress
			if(restartType == RestartType.TO_FORTRESS && clan.getHasFortress() != 0)
				return ResidenceHolder.getInstance().getResidence(clan.getHasFortress()).getOwnerRestartPoint();
		}

		if(player.getKarma() > 1)
		{
			if(player.getPKRestartPoint() != null)
				return player.getPKRestartPoint();
		}
		else if(player.getRestartPoint() != null)
			return player.getRestartPoint();

		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, from);
		if(ra != null)
		{
			RestartPoint rp = ra.getRestartPoint().get(player.getRace());

			Location restartPoint = Rnd.get(rp.getRestartPoints());
			Location PKrestartPoint = Rnd.get(rp.getPKrestartPoints());

			return player.getKarma() > 1 ? PKrestartPoint : restartPoint;
		}

		_log.warn("Cannot find restart location from coordinates: " + from + "!");

		return DEFAULT_RESTART;
	}
}