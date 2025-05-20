package handler.items;

import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExGetBookMarkInfo;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class TeleportBookmark extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 13015, 13301 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		if(player == null || item == null || !(player instanceof Player))
			return false;

		if(player.bookmarks.getCapacity() >= 30)
		{
			player.sendPacket(SystemMsg.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT);
			return false;
		}
		else
		{
			player.getInventory().destroyItem(item, 1);
			player.sendPacket(new SystemMessage2(SystemMsg.S1_HAS_DISAPPEARED).addItemName(item.getItemId()));
			player.bookmarks.setCapacity(player.bookmarks.getCapacity() + 3);
			player.sendPacket(SystemMsg.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
			player.broadCast(new ExGetBookMarkInfo(player));
			return true;
		}
	}
}