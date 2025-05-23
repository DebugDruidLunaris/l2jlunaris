package jts.gameserver.network.clientpackets;

import jts.commons.math.SafeMath;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.model.items.Warehouse;
import jts.gameserver.model.items.Warehouse.WarehouseType;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);

	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
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
			_items[i] = readD(); // item object id
			_itemQ[i] = readQ(); // count
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;

		if(!activeChar.getPlayerAccess().UseWarehouse)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		NpcInstance whkeeper = activeChar.getLastNpc();
		if(activeChar.isBBSUse() && (whkeeper == null || !activeChar.isInRange(whkeeper, Creature.INTERACTION_DISTANCE)))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}

		Warehouse warehouse = null;
		String logType = null;

		if(activeChar.getUsingWarehouseType() == WarehouseType.PRIVATE)
		{
			warehouse = activeChar.getWarehouse();
			logType = Log.WarehouseWithdraw;
		}
		else if(activeChar.getUsingWarehouseType() == WarehouseType.CLAN)
		{
			logType = Log.ClanWarehouseWithdraw;
			boolean canWithdrawCWH = false;
			if(activeChar.getClan() != null)
				if((activeChar.getClanPrivileges() & Clan.CP_CL_WAREHOUSE_SEARCH) == Clan.CP_CL_WAREHOUSE_SEARCH && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.isClanLeader() || activeChar.getVarB("canWhWithdraw")))
					canWithdrawCWH = true;
			if(!canWithdrawCWH)
				return;

			warehouse = activeChar.getClan().getWarehouse();
		}
		else if(activeChar.getUsingWarehouseType() == WarehouseType.FREIGHT)
		{
			warehouse = activeChar.getFreight();
			logType = Log.FreightWithdraw;
		}
		else
		{
			_log.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
			return;
		}

		PcInventory inventory = activeChar.getInventory();

		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			long weight = 0;
			int slots = 0;

			for(int i = 0; i < _count; i++)
			{
				ItemInstance item = warehouse.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i])
				{
					activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}

				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getTemplate().getWeight(), _itemQ[i]));
				if(!item.isStackable() || inventory.getItemByItemId(item.getItemId()) == null) // вещь требует слота
					slots++;
			}

			if(!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			for(int i = 0; i < _count; i++)
			{
				ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);
				Log.LogItem(activeChar, logType, item);
				Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "WareHouse", new String[] { "WareHouse WithDraw Item:", "" + item.getName() + " count: " + item.getCount() + " objId: " + item.getObjectId() + "" });
				activeChar.getInventory().addItem(item);
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			warehouse.writeUnlock();
			inventory.writeUnlock();
		}

		activeChar.sendChanges();
		activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
	}
}