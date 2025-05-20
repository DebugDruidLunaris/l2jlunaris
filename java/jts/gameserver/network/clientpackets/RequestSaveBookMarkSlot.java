package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExGetBookMarkInfo;
import jts.gameserver.network.serverpackets.ShortCutInit;

public class RequestSaveBookMarkSlot extends L2GameClientPacket
{
	private String name, acronym;
	private int icon;

	@Override
	protected void readImpl()
	{
		name = readS(32);
		icon = readD();
		acronym = readS(4);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null && activeChar.bookmarks.add(name, acronym, icon))
		{
			activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
			activeChar.sendPacket(new ShortCutInit(activeChar));
		}
	}
}