package jts.gameserver.network.serverpackets;

import jts.gameserver.model.GameObject;

/**
 * sample
 * 0000: 0c  9b da 12 40                                     ....@
 * format  d
 */
public class Revive extends L2GameServerPacket
{
	private int _objectId;

	public Revive(GameObject obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x01);
		writeD(_objectId);
	}
}