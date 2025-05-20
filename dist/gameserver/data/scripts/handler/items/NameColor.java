package handler.items;

import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExChangeNicknameNColor;

public class NameColor extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 13021, 13307 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		player.sendPacket(new ExChangeNicknameNColor(item.getObjectId()));
		return true;
	}
}