package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.Recipe;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private int _id;
	private boolean _isDwarvenRecipe;
	private int _status;
	private int _curMP;
	private int _maxMP;

	public RecipeItemMakeInfo(Player player, Recipe recipeList, int status)
	{
		_id = recipeList.getId();
		_isDwarvenRecipe = recipeList.isDwarvenRecipe();
		_status = status;
		_curMP = (int) player.getCurrentMp();
		_maxMP = player.getMaxMp();
		//
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xdd);
		writeD(_id); //ID рецепта
		writeD(_isDwarvenRecipe ? 0x00 : 0x01);
		writeD(_curMP);
		writeD(_maxMP);
		writeD(_status); //итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
	}
}