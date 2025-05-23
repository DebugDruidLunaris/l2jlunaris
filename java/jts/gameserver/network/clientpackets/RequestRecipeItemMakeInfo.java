package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.RecipeHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.Recipe;
import jts.gameserver.network.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;

	/**
	 * packet type id 0xB7
	 * format:		cd
	 */
	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Recipe recipeList = RecipeHolder.getInstance().getRecipeByRecipeId(_id);
		if(recipeList == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		sendPacket(new RecipeItemMakeInfo(activeChar, recipeList, 0xffffffff));
	}
}