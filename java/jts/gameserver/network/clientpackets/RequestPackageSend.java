package jts.gameserver.network.clientpackets;

import jts.commons.math.SafeMath;
import jts.gameserver.Config;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcFreight;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

import org.apache.commons.lang3.ArrayUtils;

public class RequestPackageSend extends L2GameClientPacket
{
	private static final long _FREIGHT_FEE = 1000; //TODO [VISTALL] hardcode price

	private int _objectId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || _count == 0)
			return;

		if(!player.getPlayerAccess().UseWarehouse)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode())
		{
			player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}

		// Проверяем наличие npc и расстояние до него
		NpcInstance whkeeper = player.getLastNpc();
		if(whkeeper == null || !player.isInRangeZ(whkeeper, Creature.INTERACTION_DISTANCE))
			return;

		if(!player.getAccountChars().containsKey(_objectId))
			return;

		PcInventory inventory = player.getInventory();
		PcFreight freight = new PcFreight(_objectId);
		freight.restore();

		inventory.writeLock();
		freight.writeLock();
		try
		{
			int slotsleft = 0;
			long adenaDeposit = 0;

			slotsleft = Config.FREIGHT_SLOTS - freight.getSize();

			int items = 0;

			// Создаем новый список передаваемых предметов, на основе полученных данных
			for(int i = 0; i < _count; i++)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i] || !item.getTemplate().isFreightable())
				{
					_items[i] = 0; // Обнуляем, вещь не будет передана
					_itemQ[i] = 0L;
					continue;
				}

				if(!item.isStackable() || freight.getItemByItemId(item.getItemId()) == null) // вещь требует слота
				{
					if(slotsleft <= 0) // если слоты кончились нестекуемые вещи и отсутствующие стекуемые пропускаем
					{
						_items[i] = 0; // Обнуляем, вещь не будет передана
						_itemQ[i] = 0L;
						continue;
					}
					slotsleft--; // если слот есть то его уже нет
				}

				if(item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
					adenaDeposit = _itemQ[i];

				items++;
			}

			// Сообщаем о том, что слоты кончились
			if(slotsleft <= 0)
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);

			if(items == 0)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}

			// Проверяем, хватит ли у нас денег на уплату налога
			long fee = SafeMath.mulAndCheck(items, _FREIGHT_FEE);

			if(fee + adenaDeposit > player.getAdena())
			{
				player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}

			if(!player.reduceAdena(fee, true))
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}

			for(int i = 0; i < _count; i++)
			{
				if(_items[i] == 0)
					continue;
				ItemInstance item = inventory.removeItemByObjectId(_items[i], _itemQ[i]);
				Log.LogItem(player, Log.FreightDeposit, item);
				Log_New.LogEvent(player.getName(), player.getIP(), "Freight", new String[] { "Freight send item:", "" + item.getName() + " count: " + item.getCount() + " objId: " + item.getObjectId() + "" });
				freight.addItem(item);
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			freight.writeUnlock();
			inventory.writeUnlock();
		}

		// Обновляем параметры персонажа
		player.sendChanges();
		player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
}