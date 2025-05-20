package jts.gameserver.model.items.attachment;

import jts.gameserver.model.Player;

public interface PickableAttachment extends ItemAttachment
{
	boolean canPickUp(Player player);
	void pickUp(Player player);
}