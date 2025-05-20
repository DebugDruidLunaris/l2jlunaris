package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

public class RequestDestroyItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

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

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		long count = _count;

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(count < 1)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
			return;
		}

		if(!activeChar.isGM() && item.isHeroWeapon())
		{
			activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			return;
		}

		if(activeChar.getPet() != null && activeChar.getPet().getControlItemObjId() == item.getObjectId())
		{
			activeChar.sendPacket(Msg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
			return;
		}

		if(!activeChar.isGM() && !item.canBeDestroyed(activeChar))
		{
			activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}

		if(_count > item.getCount())
			count = item.getCount();

		boolean crystallize = item.canBeCrystallized(activeChar);

		int crystalId = item.getTemplate().getCrystalType().cry;
		int crystalAmount = item.getTemplate().getCrystalCount();
		if(crystallize)
		{
			int level = activeChar.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
			if(level < 1 || crystalId - ItemTemplate.CRYSTAL_D + 1 > level)
				crystallize = false;
		}

		Log.LogItem(activeChar, Log.Delete, item, count);
		Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "DestroyItem", new String[] { "Destroyed Item:", item.getName(), "(objId: " + this._objectId + ")", "count: " + count + "" });
		if(!activeChar.getInventory().destroyItemByObjectId(_objectId, count))
		{
			activeChar.sendActionFailed();
			return;
		}

		// При удалении ошейника, удалить пета
		if(PetDataTable.isPetControlItem(item))
			PetDataTable.deletePet(item, activeChar);

		if(crystallize)
		{
			activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
			ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
		}
		else
			activeChar.sendPacket(SystemMessage2.removeItems(item.getItemId(), count));

		activeChar.sendChanges();
	}
}