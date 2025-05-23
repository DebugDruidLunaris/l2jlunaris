package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.dao.CharacterPostFriendDAO;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExConfirmAddingPostFriend;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;

import org.napile.primitive.maps.IntObjectMap;

public class RequestExAddPostFriendForPostBox extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl() throws Exception
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		int targetObjectId = CharacterDAO.getInstance().getObjectIdByName(_name);
		if(targetObjectId == 0)
		{
			player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_EXISTS));
			return;
		}

		if(_name.equalsIgnoreCase(player.getName()))
		{
			player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.NAME_IS_NOT_REGISTERED));
			return;
		}

		IntObjectMap<String> postFriend = player.getPostFriends();
		if(postFriend.size() >= Player.MAX_POST_FRIEND_SIZE)
		{
			player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.LIST_IS_FULL));
			return;
		}

		if(postFriend.containsKey(targetObjectId))
		{
			player.sendPacket(new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.ALREADY_ADDED));
			return;
		}

		CharacterPostFriendDAO.getInstance().insert(player, targetObjectId);
		postFriend.put(targetObjectId, CharacterDAO.getInstance().getNameByObjectId(targetObjectId));

		player.sendPacket(new SystemMessage2(SystemMsg.S1_WAS_SUCCESSFULLY_ADDED_TO_YOUR_CONTACT_LIST).addString(_name), new ExConfirmAddingPostFriend(_name, ExConfirmAddingPostFriend.SUCCESS));
	}
}