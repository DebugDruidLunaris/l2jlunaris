package jts.gameserver.network.serverpackets;

import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.utils.Location;

public class GetItem extends L2GameServerPacket
{
	private int _playerId, _itemObjId;
	private Location _loc;

	public GetItem(ItemInstance item, int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x17);
		writeD(_playerId);
		writeD(_itemObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}