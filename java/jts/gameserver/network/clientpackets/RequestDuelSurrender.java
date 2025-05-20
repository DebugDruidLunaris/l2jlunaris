package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.utils.Log_New;

public class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		DuelEvent duelEvent = player.getEvent(DuelEvent.class);
		if(duelEvent == null)
			return;

		duelEvent.packetSurrender(player);
		Log_New.LogEvent(player.getName(), "Duel", "SurrenderDuel", new String[] { "duel surrendered with:" });
	}
}