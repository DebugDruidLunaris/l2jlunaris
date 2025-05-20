package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.StaticObjectInstance;

public class ChairSit extends L2GameServerPacket
{
	private int _objectId;
	private int _staticObjectId;

	public ChairSit(Player player, StaticObjectInstance throne)
	{
		_objectId = player.getObjectId();
		_staticObjectId = throne.getUId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xed);
		writeD(_objectId);
		writeD(_staticObjectId);
	}
}