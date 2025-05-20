package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExShowSentPostList;

public class RequestExRequestSentPostList extends L2GameClientPacket
{
	@Override
	protected void readImpl() {} //just a trigger

	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();
		if(cha != null)
			cha.sendPacket(new ExShowSentPostList(cha));
	}
}