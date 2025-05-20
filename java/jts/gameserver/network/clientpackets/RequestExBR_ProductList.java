package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExBR_ProductList;

public class RequestExBR_ProductList extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		if(!Config.itemmallEnable)
		{
			return;
		}
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExBR_ProductList());
	}
}