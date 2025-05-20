package npc.model.residences.castle;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.CastleSiegeInfo;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class CastleMessengerInstance extends NpcInstance
{
	public CastleMessengerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		Castle castle = getCastle();

		if(player.isCastleLord(castle.getId()))
		{
			if(castle.getSiegeEvent().isInProgress())
				showChatWindow(player, "residence2/castle/sir_tyron021.htm");
			else
				showChatWindow(player, "residence2/castle/sir_tyron007.htm");
		}
		else if(castle.getSiegeEvent().isInProgress())
			showChatWindow(player, "residence2/castle/sir_tyron021.htm");
		else
			player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}