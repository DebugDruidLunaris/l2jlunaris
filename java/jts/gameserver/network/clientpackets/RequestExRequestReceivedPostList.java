package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExShowReceivedPostList;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class RequestExRequestReceivedPostList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//just a trigger
	}

	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();
		if(cha != null)
		{
			if(!cha.isInPeaceZone())
			{
				cha.sendPacket(SystemMsg.YOU_CANNOT_RECEIVE_IN_A_NONPEACE_ZONE_LOCATION);
			}
			cha.sendItemList(true);
			cha.sendItemList(false);
			cha.sendPacket(new ExShowReceivedPostList(cha));
		}
		
		
	}
}