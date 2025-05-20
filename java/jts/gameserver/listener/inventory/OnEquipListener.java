package jts.gameserver.listener.inventory;

import jts.commons.listener.Listener;
import jts.gameserver.model.Playable;
import jts.gameserver.model.items.ItemInstance;

public interface OnEquipListener extends Listener<Playable>
{
	public void onEquip(int slot, ItemInstance item, Playable actor);
	public void onUnequip(int slot, ItemInstance item, Playable actor);
}