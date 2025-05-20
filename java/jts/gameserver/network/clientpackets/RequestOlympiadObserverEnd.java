package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.utils.Log_New;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getObserverMode() == Player.OBSERVER_STARTED)
			if(activeChar.getOlympiadObserveGame() != null)
				activeChar.leaveOlympiadObserverMode(true);
		 Log_New.LogEvent(activeChar.getName(), "Olympiad", "LeftObserverMode", new String[] { "" });

	}
}