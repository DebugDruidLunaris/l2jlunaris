package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import jts.commons.lang.ArrayUtils;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInfo;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.Warehouse.ItemClassComparator;
import jts.gameserver.model.items.Warehouse.WarehouseType;

public class WareHouseDepositList extends L2GameServerPacket
{
	private int _whtype;
	private long _adena;
	private List<ItemInfo> _itemList;

	public WareHouseDepositList(Player cha, WarehouseType whtype)
	{
		_whtype = whtype.ordinal();
		_adena = cha.getAdena();

		ItemInstance[] items = cha.getInventory().getItems();
		ArrayUtils.eqSort(items, ItemClassComparator.getInstance());
		_itemList = new ArrayList<ItemInfo>(items.length);
		for(ItemInstance item : items)
			if(item.canBeStored(cha, _whtype == 1))
				_itemList.add(new ItemInfo(item));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whtype);
		writeQ(_adena);
		writeH(_itemList.size());
		for(ItemInfo item : _itemList)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}