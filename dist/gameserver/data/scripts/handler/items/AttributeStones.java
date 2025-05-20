package handler.items;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;

public class AttributeStones extends ScriptItemHandler
{
	private static final int[] _itemIds = 
	{
		9546,
		9547,
		9548,
		9549,
		9550,
		9551,
		9552,
		9553,
		9554,
		9555,
		9556,
		9557,
		10521,
		10522,
		10523,
		10524,
		10525,
		10526 
	};

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(Msg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			return false;
		}

		if(player.getEnchantScroll() != null)
			return false;

		player.setEnchantScroll(item);
		player.sendPacket(Msg.PLEASE_SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
		player.sendPacket(new ExChooseInventoryAttributeItem(item));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}