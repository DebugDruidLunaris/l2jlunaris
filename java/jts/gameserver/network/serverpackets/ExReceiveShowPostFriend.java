package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;

import org.napile.primitive.maps.IntObjectMap;

public class ExReceiveShowPostFriend extends L2GameServerPacket
{
	private IntObjectMap<String> _list;

	public ExReceiveShowPostFriend(Player player)
	{
		_list = player.getPostFriends();
	}

	@Override
	public void writeImpl()
	{
   		writeEx(0xD3);
		writeD(_list.size());
		for(String t : _list.values())
			writeS(t);
	}
}
