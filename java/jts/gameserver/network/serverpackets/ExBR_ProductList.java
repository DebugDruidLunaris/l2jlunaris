package jts.gameserver.network.serverpackets;

import java.util.Collection;

import jts.gameserver.data.xml.holder.ProductHolder;
import jts.gameserver.model.ProductItem;

public class ExBR_ProductList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0xD6);
		Collection<ProductItem> items = ProductHolder.getInstance().getAllItems();
		writeD(items.size());

		for(ProductItem template : items)
		{
			if(System.currentTimeMillis() < template.getStartTimeSale())
				continue;

			if(System.currentTimeMillis() > template.getEndTimeSale())
				continue;

			writeD(template.getProductId()); //product id
			writeH(template.getCategory()); //category 1 - enchant 2 - supplies  3 - decoration 4 - package 5 - other
			writeD(template.getPoints()); //points
			writeD(template.getTabId()); //дополнительная категория, 0 - откл, 1 - ивент, 2 - выбор дня, 3 и туда и туда. для ивентовых, слева от молла при открытии вылазит песдатое окошко
			writeD((int) (template.getStartTimeSale() / 1000)); // start sale unix date in seconds
			writeD((int) (template.getEndTimeSale() / 1000)); // end sale unix date in seconds
			writeC(127); // day week (127 = not daily goods)
			writeC(template.getStartHour()); // start hour
			writeC(template.getStartMin()); // start min
			writeC(template.getEndHour()); // end hour
			writeC(template.getEndMin()); // end min
			writeD(template.getStock()); // stock
			writeD(template.getMaxStock()); // max stock
		}
	}
}