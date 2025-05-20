package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;
import jts.gameserver.model.Player;

public interface OnPlayerEnterListener extends PlayerListener
{
	public void onPlayerEnter(Player player);
}