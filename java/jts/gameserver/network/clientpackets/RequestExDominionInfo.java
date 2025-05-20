package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.EventType;
import jts.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import jts.gameserver.network.serverpackets.ExReplyDominionInfo;
import jts.gameserver.network.serverpackets.ExShowOwnthingPos;

public class RequestExDominionInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExReplyDominionInfo());

		DominionSiegeRunnerEvent runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
		if(runnerEvent.isInProgress())
			activeChar.sendPacket(new ExShowOwnthingPos());
	}
}