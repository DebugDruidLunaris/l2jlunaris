package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.data.xml.holder.ProductHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.ProductItem;
import jts.gameserver.model.ProductItemComponent;
import jts.gameserver.network.serverpackets.ExBR_BuyProduct;
import jts.gameserver.network.serverpackets.ExBR_GamePoint;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.Log_New;

public class RequestExBR_BuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;

	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.itemmallEnable)
		{
			return;
		}
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_count > ExBR_BuyProduct.MAX_BUY_COUNT || _count < 0)
			return;

		ProductItem product = ProductHolder.getInstance().getProduct(_productId);
		if(product == null)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		if(product.isLimitedProduct() && (product.isLimit() || product.getMaxStock() - product.getStock() < _count))
		{
			//TODO правильное сообщение
			//activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_BUY_SOLD_OUT));
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		if(System.currentTimeMillis() < product.getStartTimeSale())
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_BUY_BEFORE_SALE_DATE));
			return;
		}

		if(System.currentTimeMillis() > product.getEndTimeSale())
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_BUY_AFTER_SALE_DATE));
			return;
		}

		int totalPoints = product.getPoints() * _count;

		if(totalPoints < 0)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
			return;
		}

		final long gamePointSize = activeChar.getPremiumPoints();

		if(totalPoints > gamePointSize)
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_NOT_ENOUGH_POINTS));
			return;
		}

		int totalWeight = 0;
		for(ProductItemComponent com : product.getComponents())
			totalWeight += com.getWeight();

		totalWeight *= _count; //увеличиваем вес согласно количеству

		int totalCount = 0;

		for(ProductItemComponent com : product.getComponents())
		{
			ItemTemplate item = ItemHolder.getInstance().getTemplate(com.getItemId());
			if(item == null)
			{
				activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_WRONG_PRODUCT));
				return; //what
			}
			totalCount += item.isStackable() ? 1 : com.getCount() * _count;
		}

		if(!activeChar.getInventory().validateCapacity(totalCount) || !activeChar.getInventory().validateWeight(totalWeight))
		{
			activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_INVENTORY_FULL));
			return;
		}

		activeChar.reducePremiumPoints(totalPoints);

		for(ProductItemComponent comp : product.getComponents())
		{
			activeChar.getInventory().addItem(comp.getItemId(), comp.getCount() * _count);
		Log_New.LogEvent(activeChar.getName(), "ItemMall", "buyItems", new String[] { "bought: " + comp.getCount() * _count + " of " + comp.getItemId() + "" });
		}
		activeChar.sendPacket(new ExBR_GamePoint(activeChar));
		activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.RESULT_OK));
		product.setStock(_count);
		activeChar.sendChanges();
	}
}