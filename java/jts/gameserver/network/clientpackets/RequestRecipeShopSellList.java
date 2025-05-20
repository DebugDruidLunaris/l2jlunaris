package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.RecipeShopSellList;

public class RequestRecipeShopSellList extends L2GameClientPacket
{
	int _manufacturerId;

	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
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

		Player manufacturer = (Player) activeChar.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInRangeZ(activeChar, Creature.INTERACTION_DISTANCE))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new RecipeShopSellList(activeChar, manufacturer));
	}
}