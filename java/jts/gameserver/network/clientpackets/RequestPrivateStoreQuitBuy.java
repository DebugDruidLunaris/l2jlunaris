package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.utils.Log_New;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_BUY)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		activeChar.standUp();
		activeChar.broadcastCharInfo();
		Log_New.LogEvent(activeChar.getName(), "PrivateStore", "LeftPrivateStoreBuy", new String[] { "" });
	}
}