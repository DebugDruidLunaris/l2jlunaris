package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.EnchantItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.support.EnchantScroll;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled() || player.isInStoreMode() || player.isInTrade())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		PcInventory inventory = player.getInventory();
		ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		ItemInstance scroll = player.getEnchantScroll();

		if(itemToEnchant == null || scroll == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		Log.add(player.getName() + "|Trying to put enchant|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getEnchantLevel() + "|" + itemToEnchant.getObjectId(), "enchants");
		Log_New.LogEvent(player.getName(), player.getIP(), "EnchantItem", new String[] { "try to enchant:", "item name: "+itemToEnchant.getName()+"(" + itemToEnchant.getItemId() +")"+ " Enchant Scrol ID : "+itemToEnchant.getName()+ "("+scroll.getItemId()+")" +" enchant level: " + itemToEnchant.getEnchantLevel() + " object ID: " + itemToEnchant.getObjectId()+" enchants" });
		int scrollId = scroll.getItemId();
		int itemId = itemToEnchant.getItemId();

		EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scrollId);

		if(!itemToEnchant.canBeEnchanted(enchantScroll == null) || itemToEnchant.isStackable())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.setEnchantScroll(null);
			return;
		}

		if(itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setEnchantScroll(null);
			return;
		}

		if(player.isInStoreMode())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.setEnchantScroll(null);
			return;
		}

		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		if(enchantScroll == null)
		{
			doPutOld(player, itemToEnchant, scroll);
			//player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			//player.setEnchantScroll(null);
			return;
		}

		if(enchantScroll.getItems().size() > 0)
		{
			if(!enchantScroll.getItems().contains(itemId))
			{
				player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				player.setEnchantScroll(null);
				return;
			}
		}
		else if(!enchantScroll.getGrades().contains(itemToEnchant.getCrystalType()))
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.setEnchantScroll(null);
			return;
		}

		if(enchantScroll.getMaxEnchant() != -1 && itemToEnchant.getEnchantLevel() >= enchantScroll.getMaxEnchant())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.setEnchantScroll(null);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExPutEnchantTargetItemResult.FAIL);
			player.setEnchantScroll(null);
			return;
		}

		player.sendPacket(ExPutEnchantTargetItemResult.SUCCESS);
	}

	@Deprecated
	private static void doPutOld(Player activeChar, ItemInstance itemToEnchant, ItemInstance scroll)
	{
		int crystalId = ItemFunctions.getEnchantCrystalId(itemToEnchant, scroll, null);

		if(crystalId == -1)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			activeChar.setEnchantScroll(null);
			return;
		}

		int scrollId = scroll.getItemId();

		if(scrollId == 13540 && itemToEnchant.getItemId() != 13539)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			activeChar.setEnchantScroll(null);
			return;
		}

		// ольф 21580(21581/21582)
		if((scrollId == 21581 || scrollId == 21582) && itemToEnchant.getItemId() != 21580)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			activeChar.setEnchantScroll(null);
			return;
		}

		// TODO: [pchayka] временный хардкод до улучения системы описания свитков заточки
		if(ItemFunctions.isDestructionWpnEnchantScroll(scrollId) && itemToEnchant.getEnchantLevel() >= 15 || ItemFunctions.isDestructionArmEnchantScroll(scrollId) && itemToEnchant.getEnchantLevel() >= 6)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			activeChar.setEnchantScroll(null);
			return;
		}
		int itemType = itemToEnchant.getTemplate().getType2();
		boolean fail = false;
		switch(itemToEnchant.getItemId())
		{
			case 13539:
				if(itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX_MASTER_YOGI_STAFF)
					fail = true;
				break;
			case 21580:
				if(itemToEnchant.getEnchantLevel() >= 9)
					fail = true;
				break;
			default:
				if((itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON && itemType == ItemTemplate.TYPE2_WEAPON)||
						(itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX_SHIELD_ARMOR && itemType == ItemTemplate.TYPE2_SHIELD_ARMOR)||
						(itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX_ACCESSORY && itemType == ItemTemplate.TYPE2_ACCESSORY))
					fail = true;
				break;
		}

		if(fail)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.setEnchantScroll(null);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			activeChar.setEnchantScroll(null);
			return;
		}

		activeChar.sendPacket(new ExPutEnchantTargetItemResult(1));
	}
}