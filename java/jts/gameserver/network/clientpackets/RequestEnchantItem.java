package jts.gameserver.network.clientpackets;

import jts.commons.dao.JdbcEntityState;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.EnchantItemHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.network.serverpackets.EnchantResult;
import jts.gameserver.network.serverpackets.InventoryUpdate;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.support.EnchantScroll;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);
   private static int _objectId;
   private int _catalystObjId;
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_catalystObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInTrade())
		{
			player.setEnchantScroll(null);
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode())
		{
			player.setEnchantScroll(null);
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendActionFailed();
			return;
		}

		PcInventory inventory = player.getInventory();
		inventory.writeLock();
		try
		{
			ItemInstance item = inventory.getItemByObjectId(_objectId);
			ItemInstance catalyst = _catalystObjId > 0 ? inventory.getItemByObjectId(_catalystObjId) : null;
			ItemInstance scroll = player.getEnchantScroll();

			if(item == null || scroll == null)
			{
				player.sendActionFailed();
				return;
			}

			EnchantScroll enchantScroll = EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if(enchantScroll == null)
			{
				doEnchantOld(player, item, scroll, catalyst);
				return;
			}

			if(enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(enchantScroll.getItems().size() > 0)
			{
				if(!enchantScroll.getItems().contains(item.getItemId()))
				{
					player.sendPacket(EnchantResult.CANCEL);
					player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
					player.sendActionFailed();
					return;
				}
			}
			else if(!enchantScroll.getGrades().contains(item.getCrystalType()))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
				player.sendActionFailed();
				return;
			}

			if(!item.canBeEnchanted(false))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				player.sendActionFailed();
				return;
			}

			if(!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L))
			{
				player.sendPacket(EnchantResult.CANCEL);
				player.sendActionFailed();
				return;
			}

			boolean equipped = false;
			if(equipped = item.isEquipped())
				inventory.unEquipItem(item);

			int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? 4 : 3;

			int chance = enchantScroll.getChance();
			if(item.getEnchantLevel() < safeEnchantLevel)
				chance = 100;

			if(Rnd.chance(chance))
			{
				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.setJdbcState(JdbcEntityState.UPDATED);
				item.update();

				if(equipped)
					inventory.equipItem(item);

				player.sendPacket(new InventoryUpdate().addModifiedItem(item));

				player.sendPacket(EnchantResult.SUCESS);

				if(enchantScroll.isHasVisualEffect() && item.getEnchantLevel() > 3)
					player.broadcastPacket(new MagicSkillUse(player, player, 5965, 1, 500, 1500));
			}
			else
				switch(enchantScroll.getResultType())
				{
					case CRYSTALS:
						if(item.isEquipped())
							player.sendDisarmMessage(item);

						Log.LogItem(player, Log.EnchantFail, item);
						Log_New.LogEvent(player.getName(), player.getIP(), "EnchantItem", new String[] { "Success to enchant Item:", item.getName(), "(objId: " + _objectId + ")", "to +" + item.getEnchantLevel() + "" });
						if(!inventory.destroyItem(item, 1L))
						{
							player.sendActionFailed();
							return;
						}

						int crystalId = item.getCrystalType().cry;
						if(crystalId > 0 && item.getTemplate().getCrystalCount() > 0)
						{
							int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
							if(item.getEnchantLevel() > 3)
								crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
							if(crystalAmount < 1)
								crystalAmount = 1;

							player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
							ItemFunctions.addItem(player, crystalId, crystalAmount, true);
						}
						else
							player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

						if(enchantScroll.isHasVisualEffect())
							player.broadcastPacket(new MagicSkillUse(player, player, 5949, 1, 500, 1500));
						break;
					case DROP_ENCHANT:
						item.setEnchantLevel(0);
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();

						if(equipped)
							inventory.equipItem(item);

						player.sendPacket(new InventoryUpdate().addModifiedItem(item));
						player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
						player.sendPacket(EnchantResult.BLESSED_FAILED);
						break;
					case NOTHING:
						player.sendPacket(EnchantResult.ANCIENT_FAILED);
						break;
				}
		}
		finally
		{
			inventory.writeUnlock();

			player.setEnchantScroll(null);
			player.updateStats();
		}
	}

	@Deprecated
	private static void doEnchantOld(Player player, ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
	{
		PcInventory inventory = player.getInventory();
		// Затычка, ибо клиент криво обрабатывает RequestExTryToPutEnchantSupportItem
		if(!ItemFunctions.checkCatalyst(item, catalyst))
			catalyst = null;

		if(!item.canBeEnchanted(true))
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.sendActionFailed();
			return;
		}

		int crystalId = ItemFunctions.getEnchantCrystalId(item, scroll, catalyst);

		if(crystalId == -1)
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.sendActionFailed();
			return;
		}

		int scrollId = scroll.getItemId();

		if(scrollId == 13540 && item.getItemId() != 13539)
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.sendActionFailed();
			return;
		}

		// ольф 21580(21581/21582)
		if((scrollId == 21581 || scrollId == 21582) && item.getItemId() != 21580)
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.sendActionFailed();
			return;
		}

		// TODO: [pchayka] временный хардкод до улучения системы описания свитков заточки
		if(ItemFunctions.isDestructionWpnEnchantScroll(scrollId) && item.getEnchantLevel() >= 15 || ItemFunctions.isDestructionArmEnchantScroll(scrollId) && item.getEnchantLevel() >= 6)
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			player.sendActionFailed();
			return;
		}

		int itemType = item.getTemplate().getType2();
		boolean fail = false;
		switch(item.getItemId())
		{
			case 13539:
				if(item.getEnchantLevel() >= Config.ENCHANT_MAX_MASTER_YOGI_STAFF)
					fail = true;
				break;
			case 21580:
				if(item.getEnchantLevel() >= 9)
					fail = true;
				break;
			default:
				if((item.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON && itemType == ItemTemplate.TYPE2_WEAPON) || (item.getEnchantLevel() >= Config.ENCHANT_MAX_SHIELD_ARMOR && itemType == ItemTemplate.TYPE2_SHIELD_ARMOR) || (item.getEnchantLevel() >= Config.ENCHANT_MAX_ACCESSORY && itemType == ItemTemplate.TYPE2_ACCESSORY))
					fail = true;
				break;
		}

		if(!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L))
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendActionFailed();
			return;
		}

		if(fail)
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			player.sendActionFailed();
			return;
		}

		int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;

		double chance;
		boolean premium = player.getNetConnection().getBonus() > 1;

		if(item.getEnchantLevel() < safeEnchantLevel)
			chance = 100;
		else if(itemType == ItemTemplate.TYPE2_WEAPON)
		{
			if(Config.USE_OFFLIKE_ENCHANT)
			{
				if(premium)
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL.length ? Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL[Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED.length ? Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED[Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON.length ? Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON[Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_WEAPON[item.getEnchantLevel()];

					_log.info("chance: " + chance);
				}
				else
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_WEAPON_CRYSTAL.length ? Config.OFFLIKE_ENCHANT_WEAPON_CRYSTAL[Config.OFFLIKE_ENCHANT_WEAPON_CRYSTAL.length - 1] : Config.OFFLIKE_ENCHANT_WEAPON_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_WEAPON_BLESSED.length ? Config.OFFLIKE_ENCHANT_WEAPON_BLESSED[Config.OFFLIKE_ENCHANT_WEAPON_BLESSED.length - 1] : Config.OFFLIKE_ENCHANT_WEAPON_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_WEAPON.length ? Config.OFFLIKE_ENCHANT_WEAPON[Config.OFFLIKE_ENCHANT_WEAPON.length - 1] : Config.OFFLIKE_ENCHANT_WEAPON[item.getEnchantLevel()];
				}
			}
			else
			{
				if(premium)
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.PREMIUM_ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.PREMIUM_ENCHANT_CHANCE_WEAPON;
				else
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.ENCHANT_CHANCE_WEAPON;
			}

			if(item.getTemplate().isMageWeapon() && Config.USE_OFFLIKE_ENCHANT && Config.USE_OFFLIKE_ENCHANT_MAGE_WEAPON)
				chance *= Config.USE_OFFLIKE_ENCHANT_MAGE_WEAPON_CHANCE;
		}
		else if(itemType == ItemTemplate.TYPE2_SHIELD_ARMOR)
		{
			if(Config.USE_OFFLIKE_ENCHANT)
			{
				if(premium)
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR[item.getEnchantLevel()];
				}
				else
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR_CRYSTAL.length ? Config.OFFLIKE_ENCHANT_ARMOR_CRYSTAL[Config.OFFLIKE_ENCHANT_ARMOR_CRYSTAL.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR_BLESSED.length ? Config.OFFLIKE_ENCHANT_ARMOR_BLESSED[Config.OFFLIKE_ENCHANT_ARMOR_BLESSED.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR.length ? Config.OFFLIKE_ENCHANT_ARMOR[Config.OFFLIKE_ENCHANT_ARMOR.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR[item.getEnchantLevel()];
				}
			}
			else
			{
				if(premium)
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.PREMIUM_ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.PREMIUM_ENCHANT_CHANCE_ARMOR;
				else
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.ENCHANT_CHANCE_ARMOR;
			}
		}
		else if(itemType == ItemTemplate.TYPE2_ACCESSORY)
		{
			if(Config.USE_OFFLIKE_ENCHANT)
			{
				if(premium)
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY.length ? Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY[Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY.length - 1] : Config.OFFLIKE_PREMIUM_ENCHANT_ARMOR_JEWELRY[item.getEnchantLevel()];
				}
				else
				{
					if(ItemFunctions.isCrystallEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL.length ? Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL[Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_CRYSTAL[item.getEnchantLevel()];
					else if(ItemFunctions.isBlessedEnchantScroll(scrollId))
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED.length ? Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED[Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY_BLESSED[item.getEnchantLevel()];
					else
						chance = item.getEnchantLevel() > Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY.length ? Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY[Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY.length - 1] : Config.OFFLIKE_ENCHANT_ARMOR_JEWELRY[item.getEnchantLevel()];
				}
			}
			else
			{
				if(premium)
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.PREMIUM_ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.PREMIUM_ENCHANT_CHANCE_ACCESSORY;
				else
					chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.ENCHANT_CHANCE_ACCESSORY;
			}
		}
		else
		{
			player.sendPacket(EnchantResult.CANCEL);
			player.sendActionFailed();
			return;
		}

		if(ItemFunctions.isDivineEnchantScroll(scrollId)) // Item Mall divine
			chance = 100;
		else if(ItemFunctions.isItemMallEnchantScroll(scrollId)) // Item Mall normal/ancient
			chance += 10;

		if(catalyst != null)
			chance += ItemFunctions.getCatalystPower(catalyst.getItemId());

		if(scrollId == 13540)
			chance = item.getEnchantLevel() < Config.SAFE_ENCHANT_MASTER_YOGI_STAFF ? 100 : Config.ENCHANT_CHANCE_MASTER_YOGI_STAFF;
		else if(scrollId == 15346 || scrollId == 15347 || scrollId == 15348 || scrollId == 15349 || scrollId == 15350)
			chance = item.getEnchantLevel() < Config.PC_BANG_SAFE_ENCHANT ? 100 : Config.PC_BANG_ENCHANT_MAX;
		else if(scrollId == 21581 || scrollId == 21582)
			if(player.getNetConnection().getBonus() > 1)
				chance = item.getEnchantLevel() < 3 ? 100 : Config.PREMIUM_ENCHANT_CHANCE_CRYSTAL_ARMOR;
			else
				chance = item.getEnchantLevel() < 3 ? 100 : Config.ENCHANT_CHANCE_CRYSTAL_ARMOR;

		boolean equipped = false;
		if(equipped = item.isEquipped())
			inventory.unEquipItem(item);
		if(Rnd.chance(chance))
		{
			int value = item.getEnchantLevel() + (item.isWeapon() ? Config.ENCHANT_SCROLL_LEVEL_WEAPON : item.isArmor() ? Config.ENCHANT_SCROLL_LEVEL_ARMOR : item.isAccessory() ? Config.ENCHANT_SCROLL_LEVEL_ACCESSORY : 1);

			if(item.isWeapon() && value > Config.ENCHANT_MAX_WEAPON)
				value = Config.ENCHANT_MAX_WEAPON;

			if(item.isArmor() && value > Config.ENCHANT_MAX_SHIELD_ARMOR)
				value = Config.ENCHANT_MAX_SHIELD_ARMOR;

			if(item.isAccessory() && value > Config.ENCHANT_MAX_ACCESSORY)
				value = Config.ENCHANT_MAX_ACCESSORY;

			item.setEnchantLevel(value);
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();

			if(equipped)
				inventory.equipItem(item);

			player.sendPacket(new InventoryUpdate().addModifiedItem(item));

			player.sendPacket(EnchantResult.SUCESS);

			player.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3).addName(player).addInteger(item.getEnchantLevel()).addItemName(item.getItemId()));

			for(Creature target : player.getAroundCharacters(Config.CHAT_RANGE, 500))
				if(item.getEnchantLevel() > 3 && Config.SHOW_ENCHANT_RESULT_UP_3)
					target.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3).addName(player).addInteger(item.getEnchantLevel()).addItemName(item.getItemId()));

			if(item.getEnchantLevel() > 3 && Config.SHOW_ENCHANT_EFFECT_RESULT) // Если предмет заточен больше чем на +3 и включена настройка эффекта успешной заточки, вызываем большой фаерверк.
				player.broadcastPacket(new MagicSkillUse(player, player, 21006, 1, 500, 1500));
		}
		else if(ItemFunctions.isBlessedEnchantScroll(scrollId)) // фейл, но заточка блесед
		{
			item.setEnchantLevel(Config.ENCHANT_CRYSTAL_FAILED);
			item.setJdbcState(JdbcEntityState.UPDATED);
			item.update();

			if(equipped)
				inventory.equipItem(item);

			player.sendPacket(new InventoryUpdate().addModifiedItem(item));
			player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED);
			player.sendPacket(EnchantResult.BLESSED_FAILED);
		}
		else if(ItemFunctions.isAncientEnchantScroll(scrollId) || ItemFunctions.isDestructionWpnEnchantScroll(scrollId) || ItemFunctions.isDestructionArmEnchantScroll(scrollId)) // фейл, но заточка ancient или destruction
			player.sendPacket(EnchantResult.ANCIENT_FAILED);
		else
		// фейл, разбиваем вещь
		{
			if(item.isEquipped())
				player.sendDisarmMessage(item);

			Log.LogItem(player, Log.EnchantFail, item);
			if(!inventory.destroyItem(item, 1L))
			{
				//TODO audit
				player.sendActionFailed();
				return;
			}

			if(crystalId > 0 && item.getTemplate().getCrystalCount() > 0)
			{
				int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
				if(item.getEnchantLevel() > 3)
					crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
				if(crystalAmount < 1)
					crystalAmount = 1;

				player.sendPacket(new EnchantResult(1, crystalId, crystalAmount));
				ItemFunctions.addItem(player, crystalId, crystalAmount, true);
				Log_New.LogEvent(player.getName(), player.getIP(), "EnchantItem", new String[] { "Failed to enchant Item:", item.getName(), "(objId: " + _objectId + ")", "crystals got: " + crystalAmount + "" });
			}
			else
				player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

			if(Config.SHOW_ENCHANT_EFFECT_RESULT)
				player.broadcastPacket(new MagicSkillUse(player, player, 5949, 1, 500, 1500));
		}
	}
}