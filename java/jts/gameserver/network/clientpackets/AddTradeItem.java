package jts.gameserver.network.clientpackets;

import java.util.List;

import jts.commons.math.SafeMath;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Request;
import jts.gameserver.model.Request.L2RequestType;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.TradeItem;
import jts.gameserver.network.serverpackets.SendTradeDone;
import jts.gameserver.network.serverpackets.TradeOtherAdd;
import jts.gameserver.network.serverpackets.TradeOwnAdd;
import jts.gameserver.network.serverpackets.TradeUpdate;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class AddTradeItem extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _tradeId;
	private int _objectId;
	private long _amount;

	@Override
	protected void readImpl()
	{
		_tradeId = readD(); // 1 ?
		_objectId = readD();
		_amount = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player parthner1 = getClient().getActiveChar();
		if(parthner1 == null || _amount < 1)
			return;

		Request request = parthner1.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.TRADE))
		{
			parthner1.sendActionFailed();
			return;
		}

		if(!request.isInProgress())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner1.isOutOfControl())
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		Player parthner2 = request.getOtherPlayer(parthner1);
		if(parthner2 == null)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendPacket(Msg.THAT_PLAYER_IS_NOT_ONLINE);
			parthner1.sendActionFailed();
			return;
		}

		if(parthner2.getRequest() != request)
		{
			request.cancel();
			parthner1.sendPacket(SendTradeDone.FAIL);
			parthner1.sendActionFailed();
			return;
		}

		if(request.isConfirmed(parthner1) || request.isConfirmed(parthner2))
		{
			parthner1.sendPacket(SystemMsg.YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED);
			parthner1.sendActionFailed();
			return;
		}

		ItemInstance item = parthner1.getInventory().getItemByObjectId(_objectId);
		if(item == null || !item.canBeTraded(parthner1))
		{
			parthner1.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD);
			return;
		}

		long count = Math.min(_amount, item.getCount());

		List<TradeItem> tradeList = parthner1.getTradeList();
		TradeItem tradeItem = null;

		try
		{
			for(TradeItem ti : parthner1.getTradeList())
				if(ti.getObjectId() == _objectId)
				{
					count = SafeMath.addAndCheck(count, ti.getCount());
					count = Math.min(count, item.getCount());
					ti.setCount(count);
					tradeItem = ti;
					break;
				}
		}
		catch(ArithmeticException ae)
		{
			parthner1.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		if(tradeItem == null)
		{
			// добавляем новую вещь в список
			tradeItem = new TradeItem(item);
			tradeItem.setCount(count);
			tradeList.add(tradeItem);
		}

		parthner1.sendPacket(new TradeOwnAdd(tradeItem, tradeItem.getCount()), new TradeUpdate(tradeItem, item.getCount() - tradeItem.getCount()));
		parthner2.sendPacket(new TradeOtherAdd(tradeItem, tradeItem.getCount()));
	}
}