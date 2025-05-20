package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;
import jts.gameserver.model.Player;

public interface OnPlayerExitListener extends PlayerListener
{
	public void onPlayerExit(Player player);
}