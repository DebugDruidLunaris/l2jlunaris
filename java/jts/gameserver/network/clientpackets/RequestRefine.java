package jts.gameserver.network.clientpackets;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.ShortCut;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExVariationResult;
import jts.gameserver.network.serverpackets.InventoryUpdate;
import jts.gameserver.network.serverpackets.ShortCutRegister;
import jts.gameserver.tables.AugmentationData;
import jts.gameserver.templates.item.ItemTemplate.Grade;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log_New;

public final class RequestRefine extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	private static final int GEMSTONE_B = 2132;

	// format: (ch)dddd
	private int _targetItemObjId, _refinerItemObjId, _gemstoneItemObjId;
	private long _gemstoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _gemstoneCount < 1)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			return;
		}

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null || activeChar.getLevel() < 46)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0), Msg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		if(TryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem))
		{
			int stat12 = 0x0000FFFF & targetItem.getAugmentationId();
			int stat34 = targetItem.getAugmentationId() >> 16;
			activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1), Msg.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
		}
		else
			activeChar.sendPacket(new ExVariationResult(0, 0, 0), Msg.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
	}

	@SuppressWarnings("incomplete-switch")
	boolean TryAugmentItem(Player player, ItemInstance targetItem, ItemInstance refinerItem, ItemInstance gemstoneItem)
	{
		Grade itemGrade = targetItem.getTemplate().getItemGrade();
		int lifeStoneId = refinerItem.getItemId();
		int gemstoneItemId = gemstoneItem.getItemId();

		boolean isAccessoryLifeStone = ItemFunctions.isAccessoryLifeStone(lifeStoneId);
		if(!targetItem.canBeAugmented(player, isAccessoryLifeStone))
			return false;

		if(!isAccessoryLifeStone && !ItemFunctions.isLifeStone(lifeStoneId))
			return false;

		long modifyGemstoneCount = _gemstoneCount;
		int lifeStoneLevel = ItemFunctions.getLifeStoneLevel(lifeStoneId);
		int lifeStoneGrade = isAccessoryLifeStone ? 0 : ItemFunctions.getLifeStoneGrade(lifeStoneId);

		if(isAccessoryLifeStone)
			switch(itemGrade)
			{
				case C:
					if(player.getLevel() < 46 || gemstoneItemId != GEMSTONE_D)
						return false;
					modifyGemstoneCount = 200;
					break;
				case B:
					if(player.getLevel() < 52 || gemstoneItemId != GEMSTONE_D)
						return false;
					modifyGemstoneCount = 300;
					break;
				case A:
					if(player.getLevel() < 61 || gemstoneItemId != GEMSTONE_C)
						return false;
					modifyGemstoneCount = 200;
					break;
				case S:
					if(player.getLevel() < 76 || gemstoneItemId != GEMSTONE_C)
						return false;
					modifyGemstoneCount = 250;
					break;
				case S80:
					if(player.getLevel() < 80 || gemstoneItemId != GEMSTONE_B)
						return false;
					modifyGemstoneCount = 250; //FIXME
					break;
				case S84:
					if(player.getLevel() < 84 || gemstoneItemId != GEMSTONE_B)
						return false;
					modifyGemstoneCount = 250; //FIXME
					break;
			}
		else
			switch(itemGrade)
			{
				case C:
					if(player.getLevel() < 46 || gemstoneItemId != GEMSTONE_D)
						return false;
					modifyGemstoneCount = 20;
					break;
				case B:
					if(player.getLevel() < 52 || gemstoneItemId != GEMSTONE_D)
						return false;
					modifyGemstoneCount = 30;
					break;
				case A:
					if(player.getLevel() < 61 || gemstoneItemId != GEMSTONE_C)
						return false;
					modifyGemstoneCount = 20;
					break;
				case S:
					if(player.getLevel() < 76 || gemstoneItemId != GEMSTONE_C)
						return false;
					modifyGemstoneCount = 25;
					break;
				case S80:
					if(player.getLevel() < 80 || gemstoneItemId != GEMSTONE_B)
						return false;
					if(targetItem.getTemplate().getCrystalCount() == 10394)
						modifyGemstoneCount = 36; // Icarus
					else
						modifyGemstoneCount = 28; // Dynasty - 28
					break;
				case S84:
					if(player.getLevel() < 84 || gemstoneItemId != GEMSTONE_B)
						return false;
					modifyGemstoneCount = 36;
					break;
			}

		// check if the lifestone is appropriate for this player
		switch(lifeStoneLevel)
		{
			case 1:
				if(player.getLevel() < 46)
					return false;
				break;
			case 2:
				if(player.getLevel() < 49)
					return false;
				break;
			case 3:
				if(player.getLevel() < 52)
					return false;
				break;
			case 4:
				if(player.getLevel() < 55)
					return false;
				break;
			case 5:
				if(player.getLevel() < 58)
					return false;
				break;
			case 6:
				if(player.getLevel() < 61)
					return false;
				break;
			case 7:
				if(player.getLevel() < 64)
					return false;
				break;
			case 8:
				if(player.getLevel() < 67)
					return false;
				break;
			case 9:
				if(player.getLevel() < 70)
					return false;
				break;
			case 10:
				if(player.getLevel() < 76)
					return false;
				break;
			case 11:
				if(player.getLevel() < 80)
					return false;
				break;
			case 12:
				if(player.getLevel() < 82)
					return false;
				break;
			case 13:
				if(player.getLevel() < 84)
					return false;
				break;
			case 14:
				if(player.getLevel() < 85)
					return false;
				break;
			case 15:
				if(player.getLevel() < 85)
					return false;
				break;
		}

		if(!player.getInventory().destroyItemByObjectId(_gemstoneItemObjId, modifyGemstoneCount))
			return false;

		// consume the life stone
		if(!player.getInventory().destroyItemByObjectId(_refinerItemObjId, 1L))
			return false;

		// generate augmentation
		lifeStoneLevel = Math.min(lifeStoneLevel, 10) - 1; // 10 уровней (0-9)

		int augmentation = AugmentationData.getInstance().generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade, targetItem.getTemplate().getBodyPart());

		boolean equipped = false;
		if(equipped = targetItem.isEquipped())
			player.getInventory().unEquipItem(targetItem);

		targetItem.setAugmentationId(augmentation);
		targetItem.setJdbcState(JdbcEntityState.UPDATED);
		targetItem.update();
		Log_New.LogEvent(player.getName(), "Augmentation", "SuccessAgument", new String[] { "augmented id: " + targetItem.getItemId() + " for Augument ID: " + augmentation + "" });
		if(equipped)
			player.getInventory().equipItem(targetItem);

		player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem));

		for(ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == targetItem.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM)
				player.sendPacket(new ShortCutRegister(player, sc));
		player.sendChanges();
		return true;
	}
}