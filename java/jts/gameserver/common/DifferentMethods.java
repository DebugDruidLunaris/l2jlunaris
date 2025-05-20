package jts.gameserver.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jts.gameserver.Announcements;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class DifferentMethods
{
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	public static void clear(Player player)
	{
		for(ItemInstance item : player.getInventory().getItems())
		{
			if(item.getCount() == 1)
				player.sendMessage(item.getName() + " был удален.");
			else if(item.getCount() > 1)
				player.sendMessage(item.getCount() + " " + item.getName() + " было удалено.");
			player.getInventory().destroyItemByItemId(item.getItemId(), item.getCount());
		}

		for(ItemInstance item : player.getWarehouse().getItems())
		{
			if(item.getCount() == 1)
				player.sendMessage(item.getName() + " был удален.");
			else if(item.getCount() > 1)
				player.sendMessage(item.getCount() + " " + item.getName() + " было удалено.");
			player.getWarehouse().destroyItemByItemId(item.getItemId(), item.getCount());
		}

		if(player.getClan() != null && player.isClanLeader())
			for(ItemInstance item : player.getClan().getWarehouse().getItems())
			{
				if(item.getCount() == 1)
					player.sendMessage(item.getName() + " был удален.");
				else if(item.getCount() > 1)
					player.sendMessage(item.getCount() + " " + item.getName() + " было удалено.");
				player.getClan().getWarehouse().destroyItemByItemId(item.getItemId(), item.getCount());
			}

		player.sendMessage("За подмену данных все предметы были удалены.");
	}

	public static void communityNextPage(Player player, String link)
	{
		String bypass = link;
		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(bypass);
		if(handler != null)
			handler.onBypassCommand(player, bypass);
	}

	public static String time()
	{
		return TIME_FORMAT.format(new Date(System.currentTimeMillis()));
	}

	public static boolean getPay(Player player, int itemid, long count, boolean sendMessage)
	{
		int enoughItemCount = (int) (count - player.getInventory().getCountOf(itemid));

		if(count == 0)
			return true;

		if(player.getInventory().getCountOf(itemid) < count)
		{
			if(sendMessage)
			{
				player.sendPacket(new ExShowScreenMessage(new CustomMessage("communityboard.enoughItemCount", player).addNumber(enoughItemCount).addItemName(itemid).toString(), 3000, ScreenMessageAlign.TOP_CENTER, true));
				player.sendMessage(new CustomMessage("communityboard.enoughItemCount", player).addNumber(enoughItemCount).addItemName(itemid));
			}
			return false;
		}
		else
		{
			player.getInventory().destroyItemByItemId(itemid, count);
			if(sendMessage)
				player.sendMessage(player.isLangRus() ? "Исчезло: " + count + " " + getItemName(itemid) + "." : "Disappeared: " + count + " " + getItemName(itemid) + ".");
			return true;
		}
	}

	public static void addItem(Player player, int itemid, long count)
	{
		player.getInventory().addItem(itemid, count);
		player.sendMessage("Вы получили " + count + " " + getItemName(itemid));
	}

	public static long addMinutes(long count)
	{
		long MINUTE = count * 1000 * 60;
		return MINUTE;
	}

	public static long addDay(long count)
	{
		long DAY = count * 1000 * 60 * 60 * 24;
		return DAY;
	}

	public static String getItemName(int itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getName();
	}

	public static String declension(Player player, int count, String Type)
	{
		String one = "";
		String two = "";
		String five = "";

		if(Type.equals("Days"))
		{
			one = new CustomMessage("common.declension.day.1", player).toString();
			two = new CustomMessage("common.declension.day.2", player).toString();
			five = new CustomMessage("common.declension.day.5", player).toString();
		}

		if(Type.equals("Hour"))
		{
			one = new CustomMessage("common.declension.hour.1", player).toString();
			two = new CustomMessage("common.declension.hour.2", player).toString();
			five = new CustomMessage("common.declension.hour.5", player).toString();
		}

		if(Type.equals("Piece"))
		{
			one = new CustomMessage("common.declension.piece.1", player).toString();
			two = new CustomMessage("common.declension.piece.2", player).toString();
			five = new CustomMessage("common.declension.piece.5", player).toString();
		}

		if(count > 100)
			count %= 100;

		if(count > 20)
			count %= 10;

		switch(count)
		{
			case 1:
				return one.toString();
			case 2:
			case 3:
			case 4:
				return two.toString();
			default:
				return five.toString();
		}
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
	}
}