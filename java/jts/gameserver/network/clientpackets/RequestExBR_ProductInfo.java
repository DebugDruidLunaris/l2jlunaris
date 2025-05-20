package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExBR_ProductInfo;

public class RequestExBR_ProductInfo extends L2GameClientPacket
{
	private int _productId;

	@Override
	protected void readImpl()
	{
		_productId = readD();
	}

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

		activeChar.sendPacket(new ExBR_ProductInfo(_productId));
	}
}