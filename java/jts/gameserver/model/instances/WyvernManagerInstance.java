package jts.gameserver.model.instances;

import java.util.StringTokenizer;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public final class WyvernManagerInstance extends NpcInstance
{
	public WyvernManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		boolean condition = validateCondition(player);

		if(actualCommand.equalsIgnoreCase("RideHelp"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/help_ride.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
		}
		if(condition)
		{
			if(actualCommand.equalsIgnoreCase("RideWyvern") && player.isClanLeader())
				if(!player.isRiding() || !PetDataTable.isStrider(player.getMountNpcId()))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/not_ready.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 25)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/havenot_cry.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/no_ride_dusk.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(player.getInventory().destroyItemByItemId(1460, 25L))
				{
					player.setMount(PetDataTable.WYVERN_ID, player.getMountObjId(), player.getMountLevel());
					NpcHtmlMessage html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/after_ride.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
		}

		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(!validateCondition(player))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/lord_only.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("wyvern/lord_here.htm");
		html.replace("%Char_name%", String.valueOf(player.getName()));
		html.replace("%npcname%", "Wyvern Manager " + getName());
		player.sendPacket(html);
		player.sendActionFailed();
	}

	private boolean validateCondition(Player player)
	{
		Residence residence = getCastle();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		residence = getFortress();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		residence = getClanHall();
		if(residence != null && residence.getId() > 0)
			if(player.getClan() != null)
				if(residence.getOwnerId() == player.getClanId() && player.isClanLeader()) // Leader of clan
					return true; // Owner
		return false;
	}
}