package npc.model.residences.clanhall;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.CastleSiegeInfo;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class MessengerInstance extends NpcInstance
{
	private String _siegeDialog;
	private String _ownerDialog;

	public MessengerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_siegeDialog = template.getAIParams().getString("siege_dialog");
		_ownerDialog = template.getAIParams().getString("owner_dialog");
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();
		if(clanHall.getOwner() != null && clanHall.getOwner() == player.getClan())
			showChatWindow(player, _ownerDialog);
		else if(siegeEvent.isInProgress())
			showChatWindow(player, _siegeDialog);
		else
			player.sendPacket(new CastleSiegeInfo(clanHall, player));
	}
}