package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;
import jts.gameserver.model.Player;

public interface OnPlayerPartyLeaveListener extends PlayerListener
{
	public void onPartyLeave(Player player);
}