package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.BookMark;
import jts.gameserver.network.serverpackets.ExGetBookMarkInfo;
import jts.gameserver.network.serverpackets.ShortCutInit;

public class RequestModifyBookMarkSlot extends L2GameClientPacket
{
	private String name, acronym;
	private int icon, slot;

	@Override
	protected void readImpl()
	{
		slot = readD();
		name = readS(32);
		icon = readD();
		acronym = readS(4);
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			final BookMark mark = activeChar.bookmarks.get(slot);
			if(mark != null)
			{
				mark.setName(name);
				mark.setIcon(icon);
				mark.setAcronym(acronym);
				activeChar.sendPacket(new ExGetBookMarkInfo(activeChar));
				activeChar.sendPacket(new ShortCutInit(activeChar));
			}
		}
	}
}