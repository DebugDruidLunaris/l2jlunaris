package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.Request;
import jts.gameserver.model.Request.L2RequestType;
import jts.gameserver.model.matching.MatchingRoom;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Request request = activeChar.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.PARTY_ROOM))
			return;

		if(!request.isInProgress())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			activeChar.sendActionFailed();
			return;
		}

		if(requestor.getRequest() != request)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		// отказ
		if(_response == 0)
		{
			request.cancel();
			requestor.sendPacket(SystemMsg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
			return;
		}

		if(activeChar.getMatchingRoom() != null)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		try
		{
			MatchingRoom room = requestor.getMatchingRoom();
			if(room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
				return;

			room.addMember(activeChar);
		}
		finally
		{
			request.done();
		}
	}
}