package handler.items;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExAutoSoulShot;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.item.WeaponTemplate;
import jts.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FishShots extends ScriptItemHandler
{
	// All the item IDs that this handler knows.
	private static int[] _itemIds = { 6535, 6536, 6537, 6538, 6539, 6540 };
	private static int[] _skillIds = { 2181, 2182, 2183, 2184, 2185, 2186 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;
		int FishshotId = item.getItemId();

		boolean isAutoSoulShot = false;
		if(player.getAutoSoulShot().contains(FishshotId))
			isAutoSoulShot = true;

		ItemInstance weaponInst = player.getActiveWeaponInstance();
		WeaponTemplate weaponItem = player.getActiveWeaponItem();

		if(weaponInst == null || weaponItem.getItemType() != WeaponType.ROD)
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.CANNOT_USE_SOULSHOTS);
			return false;
		}

		// spiritshot is already active
		if(weaponInst.getChargedFishshot())
			return false;

		if(item.getCount() < 1)
		{
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(FishshotId);
				player.sendPacket(new ExAutoSoulShot(FishshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(item.getName()));
				return false;
			}
			player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
			return false;
		}

		int grade = weaponItem.getCrystalType().externalOrdinal;

		if(grade == 0 && FishshotId != 6535 || grade == 1 && FishshotId != 6536 || grade == 2 && FishshotId != 6537 || grade == 3 && FishshotId != 6538 || grade == 4 && FishshotId != 6539 || grade == 5 && FishshotId != 6540)
		{
			if(isAutoSoulShot)
				return false;
			player.sendPacket(Msg.THIS_FISHING_SHOT_IS_NOT_FIT_FOR_THE_FISHING_POLE_CRYSTAL);
			return false;
		}

		if(player.getInventory().destroyItem(item, 1L))
		{
			weaponInst.setChargedFishshot(true);
			player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
			player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0));
		}
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}