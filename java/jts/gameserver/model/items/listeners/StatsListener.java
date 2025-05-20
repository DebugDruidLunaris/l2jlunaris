package jts.gameserver.model.items.listeners;

import jts.gameserver.listener.inventory.OnEquipListener;
import jts.gameserver.model.Playable;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.stats.funcs.Func;

public final class StatsListener implements OnEquipListener
{
	private static final StatsListener _instance = new StatsListener();

	public static StatsListener getInstance()
	{
		return _instance;
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		actor.removeStatsOwner(item);
		actor.updateStats();
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		Func[] funcs = item.getStatFuncs();
		actor.addStatFuncs(funcs);
		actor.updateStats();
	}
}