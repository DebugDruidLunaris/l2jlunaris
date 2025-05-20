package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.PackageSendableList;

public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl() throws Exception
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new PackageSendableList(_objectId, player));
	}
}