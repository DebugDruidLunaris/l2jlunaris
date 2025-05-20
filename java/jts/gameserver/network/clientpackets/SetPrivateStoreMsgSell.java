package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private String _storename;

	@Override
	protected void readImpl()
	{
		_storename = readS(32);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setSellStoreName(_storename);
		String checkName = _storename;
		if(Config.ENABLE_TRADE_BLOCKSPAM)
		{
			checkName = checkName.replace( " ", "" );
			for(String symbol : Config.TRADE_LIST_SYMBOLS)
			{
				checkName = checkName.replace( ""+symbol+"", "" );
			}
			
			for(String nameBlock : Config.TRADE_LIST)
			{
				if(checkName.toLowerCase().contains(nameBlock))
				{
					if(activeChar.getVar("lang@").equals("ru"))
					{
					activeChar.sendMessage("Запрещенное описание для Торговой Лавки.");
					}
					if(activeChar.getVar("lang@").equals("en"))
					{
						activeChar.sendMessage("Prohibited description for retail shops.");
					}
					return;
				}
			}
	}
	}
}