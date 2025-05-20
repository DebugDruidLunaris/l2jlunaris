package npc.model.residences.castle;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class VenomTeleporterInstance extends NpcInstance
{
	public VenomTeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		Castle castle = getCastle();
		if(castle.getSiegeEvent().isInProgress())
			showChatWindow(player, "residence2/castle/rune_massymore_teleporter002.htm");
		else
			player.teleToLocation(12589, -49044, -3008);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "residence2/castle/rune_massymore_teleporter001.htm");
	}
}