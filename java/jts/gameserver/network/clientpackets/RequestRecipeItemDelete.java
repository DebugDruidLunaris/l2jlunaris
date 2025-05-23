package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.Recipe;
import jts.gameserver.network.serverpackets.RecipeBookItemList;

public class RequestRecipeItemDelete extends L2GameClientPacket
{
	private int _recipeId;

	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE)
		{
			activeChar.sendActionFailed();
			return;
		}

		Recipe rp = RecipeHolder.getInstance().getRecipeByRecipeId(_recipeId);
		if(rp == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.unregisterRecipe(_recipeId);
		activeChar.sendPacket(new RecipeBookItemList(activeChar, rp.isDwarvenRecipe()));
	}
}