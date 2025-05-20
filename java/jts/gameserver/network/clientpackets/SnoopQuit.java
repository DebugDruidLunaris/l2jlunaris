package jts.gameserver.network.clientpackets;

import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;

public class SnoopQuit extends L2GameClientPacket
{
	private int _snoopID;

	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = (Player) GameObjectsStorage.findObject(_snoopID);
		if(player == null)
			return;
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		player.removeSnooper(activeChar);
		activeChar.removeSnooped(player);
	}
}