package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExListMpccWaiting;

public class RequestExListMpccWaiting extends L2GameClientPacket
{
	private int _listId;
	private int _locationId;
	private boolean _allLevels;

	@Override
	protected void readImpl() throws Exception
	{
		_listId = readD();
		_locationId = readD();
		_allLevels = readD() == 1;
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new ExListMpccWaiting(player, _listId, _locationId, _allLevels));
	}
}