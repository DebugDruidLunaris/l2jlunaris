package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.TradeItem;
import jts.gameserver.templates.item.ItemTemplate;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private int _sellerId;
	private long _adena;
	private boolean _package;
	private List<TradeItem> _sellList;
	private List<TradeItem> _sellList0;

	/**
	 * Окно управления личным магазином продажи
	 * @param seller
	 */
	public PrivateStoreManageListSell(Player seller, boolean pkg)
	{
		_sellerId = seller.getObjectId();
		_adena = seller.getAdena();
		_package = pkg;
		_sellList0 = seller.getSellList(_package);
		_sellList = new ArrayList<TradeItem>();

		// Проверяем список вещей в инвентаре, если вещь остутствует - убираем из списка продажи
		for(TradeItem si : _sellList0)
		{
			if(si.getCount() <= 0)
			{
				_sellList0.remove(si);
				continue;
			}

			ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
			if(item == null)
				//вещь недоступна, пробуем найти такую же по itemId
				item = seller.getInventory().getItemByItemId(si.getItemId());

			if(item == null || !item.canBeTraded(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			{
				_sellList0.remove(si);
				continue;
			}

			//корректируем количество
			si.setCount(Math.min(item.getCount(), si.getCount()));
		}

		ItemInstance[] items = seller.getInventory().getItems();
		// Проверяем список вещей в инвентаре, если вещь остутствует в списке продажи, добавляем в список доступных для продажи
		loop: for(ItemInstance item : items)
			if(item.canBeTraded(seller) && item.getItemId() != ItemTemplate.ITEM_ID_ADENA)
			{
				for(TradeItem si : _sellList0)
					if(si.getObjectId() == item.getObjectId())
					{
						if(si.getCount() == item.getCount())
							continue loop;
						// Показывает остаток вещей для продажи
						TradeItem ti = new TradeItem(item);
						ti.setCount(item.getCount() - si.getCount());
						_sellList.add(ti);
						continue loop;
					}
				_sellList.add(new TradeItem(item));
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xA0);
		//section 1
		writeD(_sellerId);
		writeD(_package ? 1 : 0);
		writeQ(_adena);

		//Список имеющихся вещей
		writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeItemInfo(si);
			writeQ(si.getStorePrice());
		}

		//Список вещей уже поставленых на продажу
		writeD(_sellList0.size());
		for(TradeItem si : _sellList0)
		{
			writeItemInfo(si);
			writeQ(si.getOwnersPrice());
			writeQ(si.getStorePrice());
		}
	}
}