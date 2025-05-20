package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;
import jts.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener
{
	public void onPartyInvite(Player player);
}