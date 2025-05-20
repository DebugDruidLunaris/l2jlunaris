package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.KrateisCubeEvent;

public class RequestExStopShowCrataeCubeRank extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception {}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		KrateisCubeEvent cubeEvent = player.getEvent(KrateisCubeEvent.class);
		if(cubeEvent == null)
			return;

		cubeEvent.closeRank(player);
	}
}