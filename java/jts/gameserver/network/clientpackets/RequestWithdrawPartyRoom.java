package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.matching.MatchingRoom;

public class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room.getId() != _roomId || room.getType() != MatchingRoom.PARTY_MATCHING)
			return;

		if(room.getLeader() == player)
			return;

		room.removeMember(player, false);
	}
}