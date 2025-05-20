package jts.gameserver.network.clientpackets;

import java.util.Map;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.actor.instances.player.Friend;
import jts.gameserver.network.serverpackets.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(Msg._FRIENDS_LIST_);
		Map<Integer, Friend> _list = activeChar.getFriendList().getList();
		for(Map.Entry<Integer, Friend> entry : _list.entrySet())
		{
			Player friend = World.getPlayer(entry.getKey());
			if(friend != null)
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_ONLINE).addName(friend));
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_OFFLINE).addString(entry.getValue().getName()));
		}
		activeChar.sendPacket(Msg.__EQUALS__);
	}
}