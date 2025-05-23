package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.network.serverpackets.ShowMiniMap;
import jts.gameserver.scripts.Functions;

public class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Map of Hellbound
		if(activeChar.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP) || activeChar.isInZone("[Hellbound_territory]") && Functions.getItemCount(activeChar, 9994) == 0)
		{
			activeChar.sendPacket(Msg.THIS_IS_AN_AREA_WHERE_YOU_CANNOT_USE_THE_MINI_MAP_THE_MINI_MAP_WILL_NOT_BE_OPENED);
			return;
		}

		sendPacket(new ShowMiniMap(activeChar, 0));
	}
}