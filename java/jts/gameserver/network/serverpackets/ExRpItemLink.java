package jts.gameserver.network.serverpackets;

import jts.gameserver.model.items.ItemInfo;

public class ExRpItemLink extends L2GameServerPacket
{
	private ItemInfo _item;

	public ExRpItemLink(ItemInfo item)
	{
		_item = item;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x6c);
		writeItemInfo(_item);
	}
}