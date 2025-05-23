package handler.items;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;

abstract class SimpleItemHandler extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if(playable.isPlayer())
			player = (Player) playable;
		else if(playable.isPet())
			player = playable.getPlayer();
		else
			return false;

		if(player.isInFlyingTransform())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}

		return useItemImpl(player, item, ctrl);
	}

	protected abstract boolean useItemImpl(Player player, ItemInstance item, boolean ctrl);

	public static boolean useItem(Player player, ItemInstance item, long count)
	{
		if(player.getInventory().destroyItem(item, count))
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(item.getItemId()));
			return true;
		}

		player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		return false;
	}

	public static boolean tryUseItem(Player player, ItemInstance item, long count, boolean showUseMsg)
	{
		if(player.getInventory().destroyItem(item, count))
		{
			if(showUseMsg)
				player.sendPacket(new SystemMessage(SystemMessage.YOU_USE_S1).addItemName(item.getItemId()));
			return true;
		}

		player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		return false;
	}

	public static boolean canBeExtracted(Player player, ItemInstance item)
	{
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL, new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return false;
		}
		return true;
	}
}