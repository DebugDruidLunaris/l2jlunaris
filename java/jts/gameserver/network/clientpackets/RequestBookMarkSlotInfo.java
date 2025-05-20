package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExGetBookMarkInfo;
import jts.gameserver.network.serverpackets.ShortCutInit;

public class RequestBookMarkSlotInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl() {} //just trigger

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
		activeChar.sendPacket(new ShortCutInit(activeChar));
	}
}