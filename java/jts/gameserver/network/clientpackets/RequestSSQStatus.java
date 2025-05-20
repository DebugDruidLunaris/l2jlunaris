package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.network.serverpackets.SSQStatus;

public class RequestSSQStatus extends L2GameClientPacket
{
	private int _page;

	@Override
	protected void readImpl()
	{
		_page = readC();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if((SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && _page == 4)
			return;

		activeChar.sendPacket(new SSQStatus(activeChar, _page));
	}
}