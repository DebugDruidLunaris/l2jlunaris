package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;

public interface OnTeleportListener extends PlayerListener
{
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection);
}