package npc.model.residences.castle;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class DoormanInstance extends npc.model.residences.DoormanInstance
{
	private Location[] _locs = new Location[2];

	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		for(int i = 0; i < _locs.length; i++)
		{
			String loc = template.getAIParams().getString("tele_loc" + i, null);
			if(loc != null)
				_locs[i] = Location.parseLoc(loc);
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
		int cond = getCond(player);
		switch(cond)
		{
			case COND_OWNER:
				if(command.equalsIgnoreCase("openDoors"))
					for(int i : _doors)
						ReflectionUtils.getDoor(i).openMe(player, true);
				else if(command.equalsIgnoreCase("closeDoors"))
					for(int i : _doors)
						ReflectionUtils.getDoor(i).closeMe(player, true);
				else if(command.startsWith("tele"))
				{
					int id = Integer.parseInt(command.substring(4, 5));
					Location loc = _locs[id];
					if(loc != null)
						player.teleToLocation(loc);
				}
				break;
			case COND_SIEGE:
				if(command.startsWith("tele"))
				{
					int id = Integer.parseInt(command.substring(4, 5));
					Location loc = _locs[id];
					if(loc != null)
						player.teleToLocation(loc);
				}
				else
					player.sendPacket(new NpcHtmlMessage(player, this, _siegeDialog, 0));
				break;
			case COND_FAIL:
				player.sendPacket(new NpcHtmlMessage(player, this, _failDialog, 0));
				break;
		}
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String filename = null;
		int cond = getCond(player);
		switch(cond)
		{
			case COND_OWNER:
			case COND_SIEGE:
				filename = _mainDialog;
				break;
			case COND_FAIL:
				filename = _failDialog;
				break;
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}

	@Override
	protected int getCond(Player player)
	{
		Castle residence = getCastle();
		Clan residenceOwner = residence.getOwner();
		if(residenceOwner != null && player.getClan() == residenceOwner && (player.getClanPrivileges() & getOpenPriv()) == getOpenPriv())
		{
			if(residence.getSiegeEvent().isInProgress() || residence.getDominion().getSiegeEvent().isInProgress())
				return COND_SIEGE;
			else
				return COND_OWNER;
		}
		else
			return COND_FAIL;
	}

	@Override
	public int getOpenPriv()
	{
		return Clan.CP_CS_ENTRY_EXIT;
	}

	@Override
	public Residence getResidence()
	{
		return getCastle();
	}
}