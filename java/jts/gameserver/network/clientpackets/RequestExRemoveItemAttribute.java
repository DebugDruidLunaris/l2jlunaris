package jts.gameserver.network.clientpackets;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.items.ItemAttributes;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.ExBaseAttributeCancelResult;
import jts.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import jts.gameserver.network.serverpackets.InventoryUpdate;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.Log_New;

public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	// Format: chd
	private int _objectId;
	private int _attributeId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_attributeId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);

		if(itemToUnnchant == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemAttributes set = itemToUnnchant.getAttributes();
		Element element = Element.getElementById(_attributeId);

		if(element == Element.NONE || set.getValue(element) <= 0)
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), ActionFail.STATIC);
			return;
		}

		// проверка делается клиентом, если зашло в эту проверку знач чит
		if(!activeChar.reduceAdena(ExShowBaseAttributeCancelWindow.getAttributeRemovePrice(itemToUnnchant), true))
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, element), SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, ActionFail.STATIC);
			return;
		}

		boolean equipped = false;
		if(equipped = itemToUnnchant.isEquipped())
			activeChar.getInventory().unEquipItem(itemToUnnchant);

		itemToUnnchant.setAttributeElement(element, 0);
		itemToUnnchant.setJdbcState(JdbcEntityState.UPDATED);
		itemToUnnchant.update();

		if(equipped)
			activeChar.getInventory().equipItem(itemToUnnchant);

		activeChar.sendPacket(new InventoryUpdate().addModifiedItem(itemToUnnchant));
		activeChar.sendPacket(new ExBaseAttributeCancelResult(true, itemToUnnchant, element));
		Log_New.LogEvent(activeChar.getName(), "Attributes", "RemoveAttribute", new String[] { "Attribute removed for: " + itemToUnnchant.getItemId() + "" });
		activeChar.updateStats();
	}
}