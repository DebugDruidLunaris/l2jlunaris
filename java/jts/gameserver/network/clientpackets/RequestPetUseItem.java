package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.PetInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.utils.ItemFunctions;

import org.apache.commons.lang3.ArrayUtils;

public class RequestPetUseItem extends L2GameClientPacket
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
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		activeChar.setActive();

		PetInstance pet = (PetInstance) activeChar.getPet();
		if(pet == null)
			return;

		ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if(item == null || item.getCount() < 1)
			return;

		if(activeChar.isAlikeDead() || pet.isDead() || pet.isOutOfControl())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			return;
		}

		// manual pet feeding
		if(pet.tryFeedItem(item))
			return;

		if(ArrayUtils.contains(Config.ALT_ALLOWED_PET_POTIONS, item.getItemId()))
		{
			Skill[] skills = item.getTemplate().getAttachedSkills();
			if(skills.length > 0)
				for(Skill skill : skills)
				{
					Creature aimingTarget = skill.getAimingTarget(pet, pet.getTarget());
					if(skill.checkCondition(pet, aimingTarget, false, false, true))
						pet.getAI().Cast(skill, aimingTarget, false, false);
				}
			return;
		}

		SystemMessage sm = ItemFunctions.checkIfCanEquip(pet, item);
		if(sm == null)
		{
			if(item.isEquipped())
				pet.getInventory().unEquipItem(item);
			else
				pet.getInventory().equipItem(item);
			pet.broadcastCharInfo();
			return;
		}

		activeChar.sendPacket(sm);
	}
}