package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExGetBookMarkInfo;
import jts.gameserver.network.serverpackets.ShortCutInit;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
	private int slot;

	@Override
	protected void readImpl()
	{
		slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			//TODO Msg.THE_SAVED_TELEPORT_LOCATION_WILL_BE_DELETED_DO_YOU_WISH_TO_CONTINUE
			activeChar.bookmarks.remove(slot);
			activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
			activeChar.sendPacket(new ShortCutInit(activeChar));
		}
	}
}