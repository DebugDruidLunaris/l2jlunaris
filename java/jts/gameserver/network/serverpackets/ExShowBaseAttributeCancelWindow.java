package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.model.Player;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.templates.item.ItemTemplate;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final List<ItemInstance> _items = new ArrayList<ItemInstance>();

	public ExShowBaseAttributeCancelWindow(Player activeChar)
	{
		for(ItemInstance item : activeChar.getInventory().getItems())
		{
			if(item.getAttributeElement() == Element.NONE || !item.canBeEnchanted(true) || getAttributeRemovePrice(item) == 0)
				continue;
			_items.add(item);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x74);
		writeD(_items.size());
		for(ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeQ(getAttributeRemovePrice(item));
		}
	}

	@SuppressWarnings("incomplete-switch")
	public static long getAttributeRemovePrice(ItemInstance item)
	{
		switch(item.getCrystalType())
		{
			case S:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 50000 : 40000;
			case S80:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 100000 : 80000;
			case S84:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 200000 : 160000;
		}
		return 0;
	}
}