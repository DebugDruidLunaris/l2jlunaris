package jts.gameserver.model.items;

import jts.gameserver.model.items.ItemInstance.ItemLocation;
import jts.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}

	@Override
	public ItemLocation getItemLocation()
	{
		return ItemLocation.CLANWH;
	}
}