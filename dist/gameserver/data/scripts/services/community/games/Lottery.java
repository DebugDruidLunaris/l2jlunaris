package services.community.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jts.commons.dbutils.DbUtils;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lottery implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(Lottery.class);
	private static int total_games;
	private static int day_games;
	private static int jackpot;
	private static Winner winner = new Winner();

	@Override
	public void onLoad()
	{
		if(Config.BBS_GAME_LOTTERY_ALLOW)
		{
			_log.info("CommunityBoard: Lottery games loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
			restoreLotteryData();
			_log.info("CommunityBoard: Lottery games played " + Util.formatAdena(getTotalGames()) + ".");
			restoreJackpot();
			_log.info("CommunityBoard: Lottery jackpot is " + Util.formatAdena(jackpot) + " " + DifferentMethods.getItemName(Config.BBS_GAME_LOTTERY_ITEM) + ".");
			restoreWinnerData();
		}
	}

	@Override
	public void onReload()
	{
		if(Config.BBS_GAME_LOTTERY_ALLOW)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{
		storeLotteryData();
		storeJackpot();
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbslottery" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/games/lottery.htm", player);
		if(bypass.startsWith("_bbslottery"))
		{
			boolean win = false;
			boolean index = false;
			boolean win_jackpot = false;
			String[] task = bypass.split(":");
			if(task[1].equals("play"))
			{
				int price = Config.BBS_GAME_LOTTERY_BET[Integer.parseInt(task[2])];
				if(DifferentMethods.getPay(player, Config.BBS_GAME_LOTTERY_ITEM, price, true))
				{
					UpdateGames();
					if(Rnd.chance(Config.BBS_GAME_LOTTERY_WIN_CHANCE))
					{
						win = true;
						Functions.addItem(player, Config.BBS_GAME_LOTTERY_ITEM, price * Config.BBS_GAME_LOTTERY_REWARD_MULTIPLE);
						if(Rnd.chance(Config.BBS_GAME_LOTTERY_JACKPOT_CHANCE))
						{
							win_jackpot = true;
							Functions.addItem(player, Config.BBS_GAME_LOTTERY_ITEM, jackpot);
							updateWinner(jackpot, player.getName());
							player.broadcastPacket(new MagicSkillUse(player, player, 6234, 1, 1000, 0));

							String[] param = {
									String.valueOf(player.getName()),
									String.valueOf(Util.formatAdena(jackpot)),
									String.valueOf(DifferentMethods.getItemName(Config.BBS_GAME_LOTTERY_ITEM)) };
							DifferentMethods.sayToAll("communityboard.games.lottery.jackpot.announce", param);
							Log.add(" " + player.getName() + " win jackpot " + jackpot + " " + DifferentMethods.getItemName(Config.BBS_GAME_LOTTERY_ITEM) + "", "CommunityBoardLottery");
							nulledJackpot();
						}
					}
					else
						setJackpot(price * Config.BBS_GAME_LOTTERY_LOOS_TO_JACKPOT / 100);
				}
			}
			else if(task[1].equals("index"))
				index = true;
			else if(task[1].equals("winner"))
			{
				index = true;
				Functions.show(showWinnerPage(player), player, null);
			}

			html = html.replace("<?lottery_result?>", index ? new CustomMessage("communityboard.games.lottery.bet.set", player).toString() : win ? new CustomMessage("communityboard.games.lottery.win", player).toString() : new CustomMessage("communityboard.games.lottery.loose", player).toString());
			html = html.replace("<?lottery_button?>", String.valueOf(button(player)));
			html = html.replace("<?lottery_jackpot?>", win_jackpot ? new CustomMessage("communityboard.games.lottery.jackpot.win", player).toString() : jackpot >= Integer.MAX_VALUE ? Util.formatAdena(jackpot) + " MAX" : Util.formatAdena(jackpot));
			html = html.replace("<?lottery_game_all?>", String.valueOf(Util.formatAdena(getTotalGames())));
			html = html.replace("<?lottery_game_day?>", String.valueOf(Util.formatAdena(getDayGames())));

		}
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(bypass) + "</center><br><br></body></html>", player);

		ShowBoard.separateAndSend(html, player);
	}

	public static String button(Player player)
	{
		StringBuilder html = new StringBuilder();
		int block = Config.BBS_GAME_LOTTERY_BET.length / 2;
		for(int i = 1; i <= Config.BBS_GAME_LOTTERY_BET.length; i++)
		{
			html.append("<td>");
			html.append("<button action=\"bypass _bbslottery:play:" + (i - 1) + "\" value=\"" + new CustomMessage("communityboard.games.lottery.bet", player).addString(Util.formatAdena(Config.BBS_GAME_LOTTERY_BET[i - 1])).toString() + "\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\">");
			html.append("</td>");
			html.append(i == block ? "</tr><tr>" : "");
		}

		return html.toString();
	}

	private static class Winner
	{
		public int[] count = new int[8];
		public String[] name = new String[8];
	}

	private String showWinnerPage(Player player)
	{
		StringBuilder html = new StringBuilder();

		html.append("<html noscrollbar>");
		html.append("<title>" + new CustomMessage("communityboard.games.lottery.top.win.title", player).toString() + "</title>");
		html.append("<body>");
		html.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
		html.append("<tr>");
		html.append("<td valign=top>");
		html.append("<table width=280 align=center height=25>");
		html.append("<tr>");
		html.append("<td valign=top width=10></td>");
		html.append("<td valign=top width=120><br>");
		html.append("<table height=25 bgcolor=808080>");
		html.append("<tr>");
		html.append("<td width=100 align=center>");
		html.append(new CustomMessage("common.name", player).toString());
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("<td valign=top width=174><br>");
		html.append("<table height=25 bgcolor=808080>");
		html.append("<tr>");
		html.append("<td width=160 align=center>");
		html.append(new CustomMessage("common.currency", player).addItemName(Config.BBS_GAME_LOTTERY_ITEM).toString());
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");

		int colorN = 0;
		String[] color = new String[] { "333333", "666666" };

		for(int i = 0; i < 8; i++)
		{
			if(winner.name[i] != null)
			{
				if(colorN > 1)
					colorN = 0;

				html.append("<table width=280 align=center height=25>");
				html.append("<tr>");
				html.append("<td valign=top width=10></td>");
				html.append("<td valign=top width=120><br>");
				html.append("<table height=25 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=100 align=center>");
				html.append("<font color=B59A75>" + winner.name[i] + "</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("<td valign=top width=174><br>");
				html.append("<table height=25 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=160 align=center>");
				html.append("<font color=LEVEL>" + Util.formatAdena(winner.count[i]) + "</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				colorN++;
			}
			else
			{
				if(colorN > 1)
					colorN = 0;

				html.append("<table width=280 align=center height=25>");
				html.append("<tr>");
				html.append("<td valign=top width=10></td>");
				html.append("<td valign=top width=120><br>");
				html.append("<table height=25 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=100 align=center>");
				html.append("<font color=B59A75>...</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("<td valign=top width=174><br>");
				html.append("<table height=25 bgcolor=" + color[colorN] + ">");
				html.append("<tr>");
				html.append("<td width=160 align=center>");
				html.append("<font color=LEVEL>...</font>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("</tr>");
				html.append("</table>");
				colorN++;
			}
		}

		html.append("<table width=280 align=center height=25>");
		html.append("<tr>");
		html.append("<td valign=top width=10></td>");
		html.append("<td alighn=center valign=top width=270><br><br>");
		html.append(new CustomMessage("communityboard.games.lottery.top.win.info", player).toString());
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");

		return html.toString();
	}

	private void restoreWinnerData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int counter = 0;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `bbs_lottery` WHERE `type`='winner' ORDER BY `count` DESC LIMIT 0,8");
			rset = statement.executeQuery();

			while(rset.next())
			{
				winner.count[counter] = rset.getInt("count");
				winner.name[counter] = rset.getString("name");
				counter++;
			}
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery winner: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private boolean restoreJackpot()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `count` FROM `bbs_lottery` WHERE `type`='jackpot'");
			rset = statement.executeQuery();

			if(rset.next())
			{
				jackpot = rset.getInt("count");
			}
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery jackpot: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return true;
	}

	private boolean restoreLotteryData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `count` FROM `bbs_lottery` WHERE `type`='total_games'");
			rset = statement.executeQuery();

			if(rset.next())
			{
				total_games = rset.getInt("count");
			}
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not restore lottery games: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return true;
	}

	private void updateWinner(int count, String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `bbs_lottery` (`count`, `type`, `name`) VALUES (" + count + ", 'winner', '" + name + "');");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery winner: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void storeJackpot()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bbs_lottery` SET `count`=" + jackpot + " WHERE `type`='jackpot'");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery jackpot: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void storeLotteryData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `bbs_lottery` SET `count`=" + getTotalGames() + " WHERE `type`='total_games'");
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.warn("CommunityBoardLottery: Could not increase current lottery games: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private int getTotalGames()
	{
		return total_games + day_games;
	}

	private int getDayGames()
	{
		return day_games;
	}

	private void UpdateGames()
	{
		day_games++;
	}

	private void setJackpot(int count)
	{
		if((jackpot + count) >= Integer.MAX_VALUE)
			jackpot = Integer.MAX_VALUE;
		else
			jackpot = jackpot + count;
	}

	private void nulledJackpot()
	{
		jackpot = Config.BBS_GAME_LOTTERY_JACKTOP_STARTED_COUNT;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}