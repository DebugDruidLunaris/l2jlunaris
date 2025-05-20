package jts.gameserver.network.serverpackets;

import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.model.items.ItemInstance;

public class PetItemList extends L2GameServerPacket
{
	private ItemInstance[] items;

	public PetItemList(PetInstance cha)
	{
		items = cha.getInventory().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);
		writeH(items.length);

		for(ItemInstance item : items)
			writeItemInfo(item);
	}
}