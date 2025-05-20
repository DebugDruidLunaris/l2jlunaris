package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.instancemanager.games.FishingChampionShipManager;
import jts.gameserver.model.Player;

public class RequestExFishRanking extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance().showMidResult(getClient().getActiveChar());
	}
}