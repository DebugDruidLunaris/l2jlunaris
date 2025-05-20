package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;

public class RequestResetNickname extends L2GameClientPacket
{
	@Override
	protected void readImpl() {} // nothing (trigger)

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getTitleColor() != Player.DEFAULT_TITLE_COLOR)
		{
			activeChar.setTitleColor(Player.DEFAULT_TITLE_COLOR);
			activeChar.broadcastUserInfo(true);
		}
	}
}