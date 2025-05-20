package handler.items;

import jts.gameserver.model.Playable;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ShowCalc;

public class Calculator extends ScriptItemHandler
{
	private static final int CALCULATOR = 4393;

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;

		playable.sendPacket(new ShowCalc(item.getItemId()));
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return new int[] { CALCULATOR };
	}
}