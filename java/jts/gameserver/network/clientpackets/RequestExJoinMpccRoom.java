package jts.gameserver.network.clientpackets;

import jts.gameserver.instancemanager.MatchingRoomManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.matching.MatchingRoom;

public class RequestExJoinMpccRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl() throws Exception
	{
		_roomId = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getMatchingRoom() != null)
			return;

		MatchingRoom room = MatchingRoomManager.getInstance().getMatchingRoom(MatchingRoom.CC_MATCHING, _roomId);
		if(room == null)
			return;

		room.addMember(player);
	}
}