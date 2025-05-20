package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;

public class RequestGoodsInventoryInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception {}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		//player.sendPacket(new ExGoodsInventoryInfo(player));
	}
}