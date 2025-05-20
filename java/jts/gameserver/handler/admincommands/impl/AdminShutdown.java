package jts.gameserver.handler.admincommands.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jts.commons.lang.StatsUtils;
import jts.gameserver.Config;
import jts.gameserver.GameTimeController;
import jts.gameserver.Shutdown;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.utils.Log_New;



import jts.gameserver.utils.Util;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminShutdown implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_server_shutdown,
		admin_server_restart,
		admin_server_abort
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanRestart)
			return false;

		try
		{
			switch(command)
			{
				case admin_server_shutdown:
					Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), Shutdown.SHUTDOWN);
					Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", new String[] { " SHUTDOWN" });
					break;
				case admin_server_restart:
					Shutdown.getInstance().schedule(NumberUtils.toInt(wordList[1], -1), Shutdown.RESTART);
					Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", new String[] { " RESTART" });
					break;
				case admin_server_abort:
					Shutdown.getInstance().cancel();
					Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", new String[] { " ABOUT" });
					break;
			}
		}
		catch(Exception e)
		{
			sendHtmlForm(activeChar);
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void sendHtmlForm(Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		int t = GameTimeController.getInstance().getGameTime();
		int h = t / 60;
		int m = t % 60;
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Главная\" action=\"bypass -h admin_admin\" width=65 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("<td width=180><center>Управления Сервером</center></td>");
		replyMSG.append("<td width=40><button value=\"Назад\" action=\"bypass -h admin_admin\" width=65 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Игроков Онлайн: " + GameObjectsStorage.getAllPlayersCount() + "</td></tr>");
	    replyMSG.append("<tr><td>Игроков Торгует: " + GameObjectsStorage.getAllOfflineCount() + "</td></tr>");
	    replyMSG.append("<tr><td>IP адресов в Игре: " + GameObjectsStorage.getWithoutSameIPCount() + "</td></tr>");
	    replyMSG.append("<tr><td>HWID данных в Игре: " + GameObjectsStorage.getWithoutSameHWIDCount() + "</td></tr>");
		replyMSG.append("<tr><td>Используеться RAM: " + StatsUtils.getMemUsedMb() + "</td></tr>");
		replyMSG.append("<tr><td>Свободной RAM: " + StatsUtils.getMemFreeMb() + "</td></tr>");
		replyMSG.append("<tr><td>Максимум RAM: " + StatsUtils.getMemMaxMb() + "</td></tr>");
		replyMSG.append("<tr><td>Рейты Сервера: " + Config.RATE_XP + "x, " + Config.RATE_SP + "x, " + Config.RATE_DROP_ADENA + "x, " + Config.RATE_DROP_ITEMS + "x</td></tr>");
		replyMSG.append("<tr><td>Игровое Время: " + format.format(cal.getTime()) + "</td></tr>");
		replyMSG.append("<tr><td>До АвтоРестарта: " + Util.formatTime2(Shutdown.getInstance().getSeconds()) + "</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Введите в секундах, время до отключения сервера ниже:</td></tr>");
		replyMSG.append("<br>");
		replyMSG.append("<tr><td><center>Секунды до: <edit var=\"shutdown_time\" width=60></center></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Выключение\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Рестарт\" action=\"bypass -h admin_server_restart $shutdown_time\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
		replyMSG.append("<button value=\"Отмена\" action=\"bypass -h admin_server_abort\" width=80 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}