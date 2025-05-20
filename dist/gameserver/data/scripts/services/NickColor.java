package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;

public class NickColor extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_CHANGE_NICK_COLOR_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}

		StringBuilder append = new StringBuilder();
		append.append("<html noscrollbar><head><title>").append(player.isLangRus() ? "Смена цвета ника" : "Change nick color").append("</title><table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td><br1>");
		append.append("<br><center> Доступные Цвета:</center><br>");
		for(String color : Config.SERVICES_CHANGE_NICK_COLOR_LIST)
		append.append("<center><a action=\"bypass -h scripts_services.NickColor:change ").append(color).append("\"><font name=\"hs10\" color=\"").append(color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2)).append("\">").append(player.getName()).append("</font></a><br1>").append(player.isLangRus() ? "Цена: " : "Price: ").append(Config.SERVICES_CHANGE_NICK_COLOR_PRICE).append("&nbsp;").append(ItemHolder.getInstance().getTemplate(Config.SERVICES_CHANGE_NICK_COLOR_ITEM).getName()).append("</center><br1>");
		append.append("<center><a action=\"bypass -h scripts_services.NickColor:change FFFFFF\"><font name=\"hs10\" color=\"FFFFFF\">").append(player.getName()).append("</font></a><br1>").append(player.isLangRus() ? "Цена: Бесплатно." : "Price: Free").append("</center><br>");
		append.append("</td></tr></table></body></html>");
		show(append.toString(), player, null);
	}

	public void change(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(param[0].equalsIgnoreCase("FFFFFF"))
		{
			player.setNameColor(Integer.decode("0xFFFFFF"));
			player.broadcastUserInfo(true);
			return;
		}

		if(player.getInventory().destroyItemByItemId(Config.SERVICES_CHANGE_NICK_COLOR_ITEM, Config.SERVICES_CHANGE_NICK_COLOR_PRICE))
		{
			player.setNameColor(Integer.decode("0x" + param[0]));
			player.broadcastUserInfo(true);
		}
		else if(Config.SERVICES_CHANGE_NICK_COLOR_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
	}
}