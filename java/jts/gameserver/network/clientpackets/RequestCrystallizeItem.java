package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	//Format: cdd

	private int _objectId;
	@SuppressWarnings("unused")
	private long unk;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		unk = readQ(); //FIXME: count??
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

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(item.isHeroWeapon())
		{
			activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			return;
		}

		if(!item.canBeCrystallized(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		int crystalAmount = item.getTemplate().getCrystalCount();
		int crystalId = item.getTemplate().getCrystalType().cry;

		//can player crystallize?
		int level = activeChar.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
		if(level < 1 || crystalId - ItemTemplate.CRYSTAL_D + 1 > level)
		{
			activeChar.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		Log.LogItem(activeChar, Log.Crystalize, item);
		 Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "Crystalization", new String[] { "Crystalized Item:", item.getName(), "(objId: " + this._objectId + ")", "to " + crystalAmount + " crystals ID: " + crystalId + "" });
		if(!activeChar.getInventory().destroyItemByObjectId(_objectId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
		ItemFunctions.addItem(activeChar, crystalId, crystalAmount, true);
		activeChar.sendChanges();
	}
}