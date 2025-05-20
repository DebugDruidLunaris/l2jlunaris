package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
	@Override
	protected void runImpl() {} // trigger

	@Override
	protected void readImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setBuyListId(0);
		activeChar.sendItemList(true);
	}
}