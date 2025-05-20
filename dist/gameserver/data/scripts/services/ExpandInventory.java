package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.item.ItemTemplate;

public class ExpandInventory extends Functions
{
	public void get()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
		{
			show("Сервис отключен.", player);
			return;
		}

		if(player.getInventoryLimit() >= Config.SERVICES_EXPAND_INVENTORY_MAX)
			if(getSelf().isLangRus())
			{
				show("<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\" color=\"FF0000\">Ошибка!</font></center><br><center>Уже максмальное количество.</center></body></html>", player);
				return;
			}
			else
			{
				show("<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\" color=\"FF0000\">Error!</font></center><br><center>Already max count.</center></body></html>", player);
				return;
			}

		if(player.getInventory().destroyItemByItemId(Config.SERVICES_EXPAND_INVENTORY_ITEM, Config.SERVICES_EXPAND_INVENTORY_PRICE))
		{
			player.setExpandInventory(player.getExpandInventory() + 1);
			player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
			player.sendMessage("Inventory capacity is now " + player.getInventoryLimit());
		}
		else if(Config.SERVICES_EXPAND_INVENTORY_ITEM == 57)
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		else
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);

		show();
	}

	public void show()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(!Config.SERVICES_EXPAND_INVENTORY_ENABLED)
			if(getSelf().isLangRus())
			{
				show("<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\" color=\"FF0000\">Ошибка!</font></center><br><center>Сервис отключен.</center></body></html>", player);
				return;
			}
			else
			{
				show("<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\" color=\"FF0000\">Error!</font></center><br><center>Service is disabled.</center></body></html>", player);
				return;
			}

		ItemTemplate item = ItemHolder.getInstance().getTemplate(Config.SERVICES_EXPAND_INVENTORY_ITEM);

		String out = "";

		if(getSelf().isLangRus())
		{
			out += "<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\">Расширение инвентаря</font></center><br>";
			out += "<br><table border=1 cellspacing=5 cellpadding=5>";
			out += "<tr><td align=center width=120>Текущий размер:</td><td align=center width=120>" + player.getInventoryLimit() + "</td></tr>";
			out += "<tr><td align=center width=120>Максимальный размер:</td><td align=center width=120>" + Config.SERVICES_EXPAND_INVENTORY_MAX + "</td></tr>";
			out += "<tr><td align=center width=120>Стоимость слота:</td><td align=center width=120>" + Config.SERVICES_EXPAND_INVENTORY_PRICE + " " + item.getName() + "</td></tr>";
			out += "</table><br><br>";
			out += "<center><button width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\" action=\"bypass -h scripts_services.ExpandInventory:get\" value=\"Расширить инвентарь\"></center>";
			out += "</body></html>";
		}
		else
		{
			out += "<html noscrollbar><title>Services Manager</title><body><center><font name=\"hs12\">Expand Inventory</font></center><br>";
			out += "<br><table border=1 cellspacing=5 cellpadding=5>";
			out += "<tr><td align=center width=120>Current Size:</td><td align=center width=120>" + player.getInventoryLimit() + "</td></tr>";
			out += "<tr><td align=center width=120>Maximum size:</td><td align=center width=120>" + Config.SERVICES_EXPAND_INVENTORY_MAX + "</td></tr>";
			out += "<tr><td align=center width=120>The cost of slots:</td><td align=center width=120>" + Config.SERVICES_EXPAND_INVENTORY_PRICE + " " + item.getName() + "</td></tr>";
			out += "</table><br><br>";
			out += "<center><button width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\" action=\"bypass -h scripts_services.ExpandInventory:get\" value=\"Expand Inventory\"></center>";
			out += "</body></html>";
		}
		show(out, player);
	}
}