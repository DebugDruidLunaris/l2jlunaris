package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jts.commons.lang.ArrayUtils;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInfo;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.Warehouse.ItemClassComparator;
import jts.gameserver.model.items.Warehouse.WarehouseType;
import jts.gameserver.templates.item.ItemTemplate.ItemClass;

public class WareHouseWithdrawList extends L2GameServerPacket
{
	private long _adena;
	private List<ItemInfo> _itemList = new ArrayList<ItemInfo>();
	private int _type;

	public WareHouseWithdrawList(Player player, WarehouseType type, ItemClass clss)
	{
		_adena = player.getAdena();
		_type = type.ordinal();

		ItemInstance[] items;
		switch(type)
		{
			case PRIVATE:
				items = player.getWarehouse().getItems(clss);
				break;
			case FREIGHT:
				items = player.getFreight().getItems(clss);
				break;
			case CLAN:
			case CASTLE:
				items = player.getClan().getWarehouse().getItems(clss);
				break;
			default:
				_itemList = Collections.emptyList();
				return;
		}

		_itemList = new ArrayList<ItemInfo>(items.length);
		ArrayUtils.eqSort(items, ItemClassComparator.getInstance());
		for(ItemInstance item : items)
			_itemList.add(new ItemInfo(item));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		writeH(_type);
		writeQ(_adena);
		writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}