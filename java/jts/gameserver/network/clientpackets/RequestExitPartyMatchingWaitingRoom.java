package jts.gameserver.network.clientpackets;

import jts.gameserver.instancemanager.MatchingRoomManager;
import jts.gameserver.model.Player;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		MatchingRoomManager.getInstance().removeFromWaitingList(player);
	}
}