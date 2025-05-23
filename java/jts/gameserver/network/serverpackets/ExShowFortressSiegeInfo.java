package jts.gameserver.network.serverpackets;

import jts.gameserver.model.entity.events.impl.FortressSiegeEvent;
import jts.gameserver.model.entity.residence.Fortress;

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
	private int _fortressId;
	private int _commandersMax;
	private int _commandersCurrent;

	public ExShowFortressSiegeInfo(Fortress fortress)
	{
		_fortressId = fortress.getId();

		FortressSiegeEvent siegeEvent = fortress.getSiegeEvent();
		_commandersMax = siegeEvent.getBarrackStatus().length;
		if(fortress.getSiegeEvent().isInProgress())
			for(int i = 0; i < _commandersMax; i++)
				if(siegeEvent.getBarrackStatus()[i])
					_commandersCurrent++;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x17);
		writeD(_fortressId);
		writeD(_commandersMax);
		writeD(_commandersCurrent);
	}
}