package handler.items;

import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.SkillTable;

public class PetSummon extends ScriptItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] _itemIds = PetDataTable.getPetControlItems();
	private static final int _skillId = 2046;

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		player.setPetControlItem(item);
		player.getAI().Cast(SkillTable.getInstance().getInfo(_skillId, 1), player, false, true);
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}
}