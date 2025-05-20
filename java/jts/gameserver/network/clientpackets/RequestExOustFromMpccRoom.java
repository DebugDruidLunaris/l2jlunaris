package jts.gameserver.network.clientpackets;

import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.matching.MatchingRoom;

public class RequestExOustFromMpccRoom extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoom room = player.getMatchingRoom();
		if(room == null || room.getType() != MatchingRoom.CC_MATCHING)
			return;

		if(room.getLeader() != player)
			return;

		Player member = GameObjectsStorage.getPlayer(_objectId);
		if(member == null)
			return;

		if(member == room.getLeader())
			return;

		room.removeMember(member, true);
	}
}