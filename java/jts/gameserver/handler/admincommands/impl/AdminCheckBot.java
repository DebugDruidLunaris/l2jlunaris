package jts.gameserver.handler.admincommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.text.TextBuilder;
import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.instancemanager.AutoHuntingManager;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.AutoHuntingPunish;
import jts.gameserver.database.DatabaseFactory;
/**
 * autor Norman
 *  02.04.2015
 */
public class AdminCheckBot implements IAdminCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(AdminCheckBot.class);
	
	private static enum Commands
	{
		admin_checkbots,
		admin_readbot,
		admin_markbotreaded,
		admin_punish_bot
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		if (!Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			activeChar.sendMessage("Бот Репорт Включен");
			return false;
		}
		
		if(!activeChar.getPlayerAccess().CanBan)
			return false;
		
		Commands command = (Commands) comm;
		
		String[] ids = fullString.split(" ");
	
		switch(command)
		{
			case admin_checkbots:
				sendBotPage(activeChar);
				break;
			case admin_readbot:
				sendBotInfoPage(activeChar, Integer.parseInt(ids[1]));
				break;
			case admin_markbotreaded:
			{
				try
				{
					AutoHuntingManager.getInstance().markAsRead(Integer.parseInt(wordList[1]));
					sendBotPage(activeChar);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			case admin_punish_bot:
			{
				activeChar.sendMessage("Комманда: //punish_bot <charName>");
				
				if (wordList != null)
				{
					Player target = GameObjectsStorage.getPlayer(wordList[1]);
					if (target != null)
					{
						synchronized (target)
						{
							int punishLevel = 0;
							try
							{
								punishLevel = AutoHuntingManager.getInstance().getPlayerReportsCount(target);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
								
							switch (punishLevel)
							{
							case 1:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.CHATBAN, 10);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_CHATTING_WILL_BE_BLOCKED_FOR_10_MINUTES));
								break;
							case 2:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 60);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_60_MINUTES));
								break;
							case 3:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_120_MINUTES));
								break;
							case 4:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.PARTYBAN, 180);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_180_MINUTES));
								break;
							case 5:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.MOVEBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_MOVEMENT_IS_PROHIBITED_FOR_120_MINUTES));
								break;
							case 6:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.ACTIONBAN, 120);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_120_MINUTES));
								break;
							case 7:
								target.setPunishDueBotting(AutoHuntingPunish.Punish.ACTIONBAN, 180);
								target.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_WILL_BE_RESTRICTED_FOR_180_MINUTES));
								break;
							default:
								activeChar.sendMessage("На вас поступила жалоба что вы ботовод!");
							}
							if (punishLevel != 0)
							{
								introduceNewPunishedBotAndClear(target);
								activeChar.sendMessage(target.getName() + " был наказан");
							}
						}
					}
					else
						activeChar.sendMessage("Цель не существует!");
				}
			}
		}
		return true;
	}
	
	private static void sendBotPage(Player activeChar)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><table width=260>");
		tb.append("<tr>");
		tb.append("<td width=60>");
		tb.append("<a action=\"bypass -h admin_admin\">Главная</a>");
		tb.append("</td>");
		tb.append("<td width=180>");
		tb.append("<center>Информация Бот Листе</center>");
		tb.append("</td>");
		tb.append("<td width=40>");
		tb.append("<a action=\"bypass -h admin_admin\">Назад</a>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<title>Списоки Бот Тикетов</title><body><center>");
		tb.append("<font color=LEVEL>Список Тикетов</font>");
		
		for (int i : AutoHuntingManager.getInstance().getUnread().keySet())
		{
			tb.append("<a action=\"bypass -h admin_readbot " + i + "\">Тикет #" + i + "</a><br1>");

		}
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	private static void sendBotInfoPage(Player activeChar, int botId)
	{
		String[] report = AutoHuntingManager.getInstance().getUnread().get(botId);
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Бот #" + botId + "</title><body><center><br>");
		tb.append("- Бот Репорт тикет ID: <font color=FF0000>" + botId + "</font><br>");
		tb.append("- Сообщил Игрок: <font color=FF0000>" + report[0] + "</font><br>");
		tb.append("- Репорт на: <font color=FF0000>" + report[1] + "</font><br>");
		tb.append("- Дата: <font color=FF0000>" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Long.parseLong(report[2])) + "</font><br>");
		tb.append("<a action=\"bypass -h admin_markbotreaded " + botId + "\">Отметить Сообщить как прочитаное</a>");
		tb.append("<a action=\"bypass -h admin_punish_bot " + report[0] + "\">Наказать " + report[0] + "</a>");
		tb.append("<a action=\"bypass -h admin_checkbots\">Вернуться к списку</a>");
		tb.append("</center></body></html>");
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	

	private static void introduceNewPunishedBotAndClear(Player target)
	{
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement delStatement = null;
		try
		{
			
			con = DatabaseFactory.getInstance().getConnection();
			// Introduce new Punished Bot in database
			statement = con.prepareStatement("INSERT INTO bot_reported_punish VALUES ( ?, ?, ? )");
			statement.setInt(1, target.getObjectId());
			statement.setString(2, target.getPlayerPunish().getBotPunishType().name());
			statement.setLong(3, target.getPlayerPunish().getPunishTimeLeft());
			statement.execute();
			
			// Delete all his reports from database
			delStatement = con.prepareStatement("DELETE FROM bot_report WHERE reported_objectId = ?");
			delStatement.setInt(1, target.getObjectId());
			delStatement.execute();
			DbUtils.closeQuietly(delStatement);
		}
		catch (Exception e)
		{
			_log.info("AdminCheckBot.introduceNewPunishedBotAndClear(target): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}