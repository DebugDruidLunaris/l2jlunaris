package services.community;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.utils.Util;

public class CommunityBoardPremiumAccount
{
	private static String _msg;

	public static String consider(Player player)
	{
		if(player.hasBonus())
		{
			int Total, Day, Hour = 0;

			Total = (int) ((player.getNetConnection().getBonusExpire() - System.currentTimeMillis() / 1000L));
			Day = Math.round(Total / 60 / 60 / 24);
			Hour = (Total - Day * 24 * 60 * 60) / 60 / 60;

			if(Day >= 1)
				_msg = new CustomMessage("communityboard.premium.day.hour", player).addNumber(Day).toString();
			else if(Day < 1 && Hour >= 0)
				_msg = new CustomMessage("communityboard.premium.hour", player).addNumber(Hour).toString();
			else
				_msg = "<font color=\"LEVEL\"><a action=\"bypass _bbsscripts:services.RateBonus:list\">" + new CustomMessage("communityboard.buy.premium", player) + "</a></font>";
		}
		else
			_msg = "<font color=\"LEVEL\"><a action=\"bypass _bbsscripts:services.RateBonus:list\">" + new CustomMessage("communityboard.buy.premium", player) + "</a></font>";

		return _msg;
	}

	public static String button(Player player)
	{
		if(player.hasBonus())
		{
			_msg = "<td><br><br><center>" + new CustomMessage("communityboard.premium.impossible", player) + "</center></td>";
		}
		else
		{
			String add = "";
			for(int i = 0; i < Config.SERVICES_RATE_BONUS_DAYS.length; i++)
			{
				add += "<td><center><button value=\"" + new CustomMessage("communityboard.cabinet.premium.button", player).addString(String.valueOf(Config.SERVICES_RATE_BONUS_VALUE[i])).addString(Config.SERVICES_RATE_BONUS_DAYS[i] + " " + DifferentMethods.declension(player, Config.SERVICES_RATE_BONUS_DAYS[i], "Days")) + "\" action=\"bypass _bbsscripts:services.RateBonus:get " + i + ";_bbshome\" width=160 height=25 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\"><br1>" + new CustomMessage("scripts.services.cost", player).addString(Util.formatAdena(Config.SERVICES_RATE_BONUS_PRICE[i])).addString(DifferentMethods.getItemName(Config.SERVICES_RATE_BONUS_ITEM[i])) + "</center></td>";
			}
			_msg = add;
		}
		return _msg;
	}

	public static String buttonCab(Player player)
	{
		if(player.hasBonus())
		{
			_msg = "<br><center>" + new CustomMessage("communityboard.premium.impossible", player) + "</center>";
		}
		else
		{
			StringBuilder html = new StringBuilder();
			String[] color = new String[] { "333333", "666666" };
			int colorN = 0;
			int block = Config.SERVICES_RATE_BONUS_DAYS.length / 2;
			for(int i = 1; i <= Config.SERVICES_RATE_BONUS_DAYS.length; i++)
			{
				if(colorN > 1)
					colorN = 0;

				html.append("<table height=50 bgcolor=" + color[colorN] + ">");
				colorN++;
				html.append("<tr>");
				html.append("<td width=250 align=center>");
				html.append("<table border=0 cellspacing=2 cellpadding=3>");
				html.append("<tr>");
				html.append("<td align=right valign=top>");
				html.append("<button action=\"bypass _bbsscripts:services.RateBonus:get " + (i - 1) + ";_bbscabinet:premium\" back=\"l2ui_ch3.PremiumItemBtn_Down\" fore=\"l2ui_ch3.PremiumItemBtn\" width=\"32\" height=\"32\"/>");
				html.append("</td>");
				html.append("<td width=204 align=left valign=top>");
				html.append("<font color=\"0099FF\">" + new CustomMessage("communityboard.cabinet.premium.button", player).addString(String.valueOf(Config.SERVICES_RATE_BONUS_VALUE[i - 1])).addString(Config.SERVICES_RATE_BONUS_DAYS[i - 1] + " " + DifferentMethods.declension(player, Config.SERVICES_RATE_BONUS_DAYS[i - 1], "Days")) + "</font>&nbsp;<br1>›&nbsp;" + new CustomMessage("scripts.services.cost", player).addString(Util.formatAdena(Config.SERVICES_RATE_BONUS_PRICE[i - 1])).addString(DifferentMethods.getItemName(Config.SERVICES_RATE_BONUS_ITEM[i - 1])) + "");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table><br>");
				html.append(i == block ? "</td><td width=250 align=center valign=top>" : "");
				if(i == block)
					colorN = 1;

			}
			_msg = html.toString();
		}
		return _msg;
	}

	public static String images(Player player)
	{
		if(player.hasBonus())
			_msg = "<img src=\"branchsys.primeitem_symbol\" width=\"14\" height=\"14\">";
		else
			_msg = "<img src=\"branchsys.br_freeserver_mark\" width=\"14\" height=\"14\">";
		return _msg;
	}
}