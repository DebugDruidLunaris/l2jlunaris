package jts.gameserver.network.serverpackets;

import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.residence.Dominion;

public class ExReplyRegisterDominion extends L2GameServerPacket
{
	private int _dominionId, _clanCount, _playerCount;
	private boolean _success, _join, _asClan;

	public ExReplyRegisterDominion(Dominion dominion, boolean success, boolean join, boolean asClan)
	{
		_success = success;
		_join = join;
		_asClan = asClan;
		_dominionId = dominion.getId();

		DominionSiegeEvent siegeEvent = dominion.getSiegeEvent();

		_playerCount = siegeEvent.getObjects(DominionSiegeEvent.DEFENDER_PLAYERS).size();
		_clanCount = siegeEvent.getObjects(SiegeEvent.DEFENDERS).size() + 1;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x91);
		writeD(_dominionId);
		writeD(_asClan);
		writeD(_join);
		writeD(_success);
		writeD(_clanCount);
		writeD(_playerCount);
	}
}