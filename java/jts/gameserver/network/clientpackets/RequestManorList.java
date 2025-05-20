package jts.gameserver.network.clientpackets;

import jts.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExSendManorList());
	}
}