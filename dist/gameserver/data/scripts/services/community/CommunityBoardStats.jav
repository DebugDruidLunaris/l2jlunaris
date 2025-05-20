package services.community;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.instancemanager.CastleManorManager;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.npc.NpcTemplate;

public class CommunityBoardStats extends Functions implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsstat" };
	}

	public static class CBStatMan
	{
		public String[] asCharNameTopPvP = new String[10];
		public int[] asCharOnlineTopPvP = new int[10];
		public int[] anCharPvPCount = new int[10];
		public String[] asCharNameTopPk = new String[10];
		public int[] asCharOnlineTopPk = new int[10];
		public int[] anCharPkCount = new int[10];
		public String[] asCharNameTopOnline = new String[10];
		public int[] anCharOnlineTime = new int[10];
		public String[] asCharNameTopRich = new String[10];
		public long[] anCharRichCount = new long[10];
		public String[] asCharNameTopClan = new String[10];
		public int[] anCharClanCount = new int[10];
		public int[] anCharClanLevel = new int[10];
		public int nHeroCount = 0;
		public int nNobleCount = 0;
		public int nHumanCount = 0;
		public int nElfCount = 0;
		public int nDarkElfCount = 0;
		public int nOrcCount = 0;
		public int nDwarfCount = 0;
		public int nKamaelCount = 0;
		public int nAllyCount = 0;
		public int nClanCount = 0;
		public int nCurrentOnline = 0;
		public int nCurrentOfftrade = 0;
		public int ClanLevel;
		public int hasCastle;
		public int ReputationClan;
		public String AllyName;
		public String ClanName;
		public String Owner;
		public String NameCastl;
		public Object siegeDate;
		public String Percent;
		public int id;

	}

	public static class CBStatRb
	{
		public String[] asRaidBossName = new String[Config.CBB_RB_LIST_STAT.length];
		public String[] anRaidBossStatus = new String[Config.CBB_RB_LIST_STAT.length];
	}

	static CBStatMan pbBStats = new CBStatMan();
	static CBStatRb pbRBStats = new CBStatRb();

	public long lUpdateTime = System.currentTimeMillis() / 1000;
	public int nCounter = 0;
	public int nAllCharCount = 0;

	@Override
	public void onBypassCommand(Player player, String command)
	{
		if(command.equals("_bbsstat:index"))
		{
			if(lUpdateTime + Config.BBS_STAT_UPDATE_TIME * 60 < System.currentTimeMillis() / 1000)
			{
				selectPlayersClassesCount();
				selectAllyCount();
				selectClanCount();
				selectHeroCount();
				selectNobleCount();
				selectCurrentOnline();
				selectTopPK(player);
				selectTopPVP(player);
				selectTopOnline(player);
				selectTopRich(player);
				selectTopClan(player);
				epicRaidBossStatus();
				lUpdateTime = System.currentTimeMillis() / 1000;
			}
			showAllStats(player, 1);
		}
		else if(command.startsWith("_bbsstat:next"))
			showAllStats(player, 2);
		else if(command.startsWith("_bbsstat:statrb"))
			showAllStats(player, 3);
		else if(command.startsWith("_bbsstat:clan"))
			showClan(player);
		else if(command.startsWith("_bbsstat:castle"))
			showCastle(player);
		else if(player.isLangRus())
			ShowBoard.separateAndSend("<html><body><br><br><center>На данный момент функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>At the moment the function: " + command + " not implemented yet</center><br><br></body></html>", player);
	}

	private void showAllStats(Player player, int nPage)
	{
		nCounter = 0;
		String content;

		if(nPage == 1)
		{
			content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stat/stats_all.htm", player);

			content = content.replace("%now_online%", Integer.toString(pbBStats.nCurrentOnline));
			content = content.replace("%now_offtrade%", Integer.toString(pbBStats.nCurrentOfftrade));

			content = content.replace("%all_noblesse%", Integer.toString(pbBStats.nNobleCount));
			content = content.replace("%all_hero%", Integer.toString(pbBStats.nHeroCount));
			content = content.replace("%all_clan%", Integer.toString(pbBStats.nClanCount));
			content = content.replace("%all_ally%", Integer.toString(pbBStats.nAllyCount));

			while(nCounter < 10)
			{
				if(pbBStats.asCharNameTopPvP[nCounter] != null)
				{
					content = content.replace("%Top_PvP_Name_" + nCounter + "%", pbBStats.asCharNameTopPvP[nCounter]);
					content = content.replace("%Top_PvP_Count_" + nCounter + "%", Integer.toString(pbBStats.anCharPvPCount[nCounter]));

					if(pbBStats.asCharOnlineTopPvP[nCounter] == 1)
						content = content.replace("%Top_PvP_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"66FF33\">да</font>" : "<font color=\"66FF33\">yes</font>");
					else
						content = content.replace("%Top_PvP_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"B59A75\">нет</font>" : "<font color=\"B59A75\">no</font>");
				}
				else
				{
					content = content.replace("%Top_PvP_Name_" + nCounter + "%", player.isLangRus() ? "Нет данных" : "No data");
					content = content.replace("%Top_PvP_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"B59A75\">нет</font>" : "<font color=\"B59A75\">no</font>");
					content = content.replace("%Top_PvP_Count_" + nCounter + "%", "0");
				}
				if(pbBStats.asCharNameTopPk[nCounter] != null)
				{
					content = content.replace("%Top_Pk_Name_" + nCounter + "%", pbBStats.asCharNameTopPk[nCounter]);
					content = content.replace("%Top_Pk_Count_" + nCounter + "%", Integer.toString(pbBStats.anCharPkCount[nCounter]));

					if(pbBStats.asCharOnlineTopPk[nCounter] == 1)
						content = content.replace("%Top_Pk_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"66FF33\">да</font>" : "<font color=\"66FF33\">yes</font>");
					else
						content = content.replace("%Top_Pk_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"B59A75\">нет</font>" : "<font color=\"B59A75\">no</font>");
				}
				else
				{
					content = content.replace("%Top_Pk_Name_" + nCounter + "%", player.isLangRus() ? "Нет данных" : "No data");
					content = content.replace("%Top_Pk_Online_" + nCounter + "%", player.isLangRus() ? "<font color=\"B59A75\">нет</font>" : "<font color=\"B59A75\">no</font>");
					content = content.replace("%Top_Pk_Count_" + nCounter + "%", "0");
				}
				nCounter++;
			}

			content = content.replace("%Human%", pbBStats.nHumanCount != 0 ? Integer.toString(pbBStats.nHumanCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Elf%", pbBStats.nElfCount != 0 ? Integer.toString(pbBStats.nElfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dark_Elf%", pbBStats.nDarkElfCount != 0 ? Integer.toString(pbBStats.nDarkElfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Orc%", pbBStats.nOrcCount != 0 ? Integer.toString(pbBStats.nOrcCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dwarf%", pbBStats.nDwarfCount != 0 ? Integer.toString(pbBStats.nDwarfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Kamael%", pbBStats.nKamaelCount != 0 ? Integer.toString(pbBStats.nKamaelCount) : player.isLangRus() ? "Нет данных" : "No data");
		}
		else if(nPage == 2)
		{
			content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stat/stats_next.htm", player);

			Locale loc = new Locale("ru", "RU");
			NumberFormat currFmt = NumberFormat.getCurrencyInstance(loc);

			while(nCounter < 10)
			{
				if(pbBStats.asCharNameTopOnline[nCounter] != null || pbBStats.anCharOnlineTime[nCounter] != 0)
				{
					content = content.replace("%Top_Online_Name_" + nCounter + "%", pbBStats.asCharNameTopOnline[nCounter]);
					content = content.replace("%Top_Online_Count_" + nCounter + "%", onlineTime(pbBStats.anCharOnlineTime[nCounter]));
				}
				else
				{
					content = content.replace("%Top_Online_Name_" + nCounter + "%", "....");
					content = content.replace("%Top_Online_Count_" + nCounter + "%", "....");
				}
				if(pbBStats.asCharNameTopRich[nCounter] != null)
				{
					content = content.replace("%Top_Rich_Name_" + nCounter + "%", pbBStats.asCharNameTopRich[nCounter]);
					content = content.replace("%Top_Rich_Count_" + nCounter + "%", currFmt.format(pbBStats.anCharRichCount[nCounter]).substring(0, currFmt.format(pbBStats.anCharRichCount[nCounter]).length() - 4));
				}
				else
				{
					content = content.replace("%Top_Rich_Name_" + nCounter + "%", "....");
					content = content.replace("%Top_Rich_Count_" + nCounter + "%", "....");
				}
				if(pbBStats.asCharNameTopClan[nCounter] != null)
				{
					content = content.replace("%Top_Clan_Name_" + nCounter + "%", pbBStats.asCharNameTopClan[nCounter]);
					content = content.replace("%Top_Clan_Count_" + nCounter + "%", Integer.toString(pbBStats.anCharClanCount[nCounter]));
					content = content.replace("%Top_Clan_Level_" + nCounter + "%", Integer.toString(pbBStats.anCharClanLevel[nCounter]));
				}
				else
				{
					content = content.replace("%Top_Clan_Name_" + nCounter + "%", "....");
					content = content.replace("%Top_Clan_Count_" + nCounter + "%", "....");
					content = content.replace("%Top_Clan_Level_" + nCounter + "%", "....");
				}
				nCounter++;
			}

			nAllCharCount = pbBStats.nHumanCount + pbBStats.nElfCount + pbBStats.nDarkElfCount + pbBStats.nOrcCount + pbBStats.nDwarfCount + pbBStats.nKamaelCount;

			content = content.replace("%Human%", nAllCharCount != 0 ? Integer.toString(pbBStats.nHumanCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Elf%", nAllCharCount != 0 ? Integer.toString(pbBStats.nElfCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dark_Elf%", nAllCharCount != 0 ? Integer.toString(pbBStats.nDarkElfCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Orc%", nAllCharCount != 0 ? Integer.toString(pbBStats.nOrcCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dwarf%", nAllCharCount != 0 ? Integer.toString(pbBStats.nDwarfCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Kamael%", nAllCharCount != 0 ? Integer.toString(pbBStats.nKamaelCount * 100 / nAllCharCount) + " %" : player.isLangRus() ? "Нет данных" : "No data");
		}
		else if(nPage == 3)
		{
			content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stat/stats_rb.htm", player);

			int i = 0;
			while(i < pbRBStats.asRaidBossName.length)
			{
				content = content.replace("<?rb_name_" + i + "?>", pbRBStats.asRaidBossName[i] != null ? pbRBStats.asRaidBossName[i] : "NULL NAME");
				content = content.replace("<?rb_status_" + i + "?>", pbRBStats.anRaidBossStatus[i] != null ? pbRBStats.anRaidBossStatus[i] : "NULL STATUS");
				i++;
			}

			content = content.replace("%Human%", pbBStats.nHumanCount != 0 ? Integer.toString(pbBStats.nHumanCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Elf%", pbBStats.nElfCount != 0 ? Integer.toString(pbBStats.nElfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dark_Elf%", pbBStats.nDarkElfCount != 0 ? Integer.toString(pbBStats.nDarkElfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Orc%", pbBStats.nOrcCount != 0 ? Integer.toString(pbBStats.nOrcCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Dwarf%", pbBStats.nDwarfCount != 0 ? Integer.toString(pbBStats.nDwarfCount) : player.isLangRus() ? "Нет данных" : "No data");
			content = content.replace("%Kamael%", pbBStats.nKamaelCount != 0 ? Integer.toString(pbBStats.nKamaelCount) : player.isLangRus() ? "Нет данных" : "No data");
		}
		else
			content = HtmCache.getInstance().getNotNull("/scripts/services/community/" + Config.BBS_FOLDER + "/in_dev.htm", player);

		ShowBoard.separateAndSend(content, player);
	}

	private void selectPlayersClassesCount()
	{
		Connection conAllPlayerCount = null;
		PreparedStatement statementAllPlayerCount = null;
		ResultSet rsAllPlayerCount = null;

		pbBStats.nHumanCount = 0;
		pbBStats.nElfCount = 0;
		pbBStats.nDarkElfCount = 0;
		pbBStats.nOrcCount = 0;
		pbBStats.nDwarfCount = 0;
		pbBStats.nKamaelCount = 0;

		try
		{
			conAllPlayerCount = DatabaseFactory.getInstance().getConnection();
			statementAllPlayerCount = conAllPlayerCount.prepareStatement("SELECT class_id FROM character_subclasses WHERE isBase = '1';");
			rsAllPlayerCount = statementAllPlayerCount.executeQuery();

			while(rsAllPlayerCount.next())
			{
				if(rsAllPlayerCount.getInt("class_id") >= 0 && rsAllPlayerCount.getInt("class_id") <= 17 || rsAllPlayerCount.getInt("class_id") >= 88 && rsAllPlayerCount.getInt("class_id") <= 98)
					pbBStats.nHumanCount++;
				if(rsAllPlayerCount.getInt("class_id") >= 18 && rsAllPlayerCount.getInt("class_id") <= 30 || rsAllPlayerCount.getInt("class_id") >= 99 && rsAllPlayerCount.getInt("class_id") <= 105)
					pbBStats.nElfCount++;
				if(rsAllPlayerCount.getInt("class_id") >= 31 && rsAllPlayerCount.getInt("class_id") <= 43 || rsAllPlayerCount.getInt("class_id") >= 106 && rsAllPlayerCount.getInt("class_id") <= 112)
					pbBStats.nDarkElfCount++;
				if(rsAllPlayerCount.getInt("class_id") >= 44 && rsAllPlayerCount.getInt("class_id") <= 52 || rsAllPlayerCount.getInt("class_id") >= 113 && rsAllPlayerCount.getInt("class_id") <= 116)
					pbBStats.nOrcCount++;
				if(rsAllPlayerCount.getInt("class_id") >= 53 && rsAllPlayerCount.getInt("class_id") <= 57 || rsAllPlayerCount.getInt("class_id") >= 117 && rsAllPlayerCount.getInt("class_id") <= 118)
					pbBStats.nDwarfCount++;
				if(rsAllPlayerCount.getInt("class_id") >= 123 && rsAllPlayerCount.getInt("class_id") <= 136)
					pbBStats.nKamaelCount++;
			}

			if(Config.CBB_ONLINE_CHEAT_ENABLE)
				if(Config.CBB_ONLINE_CHEAT_PERCENT_ENABLE)
				{
					pbBStats.nHumanCount = (pbBStats.nHumanCount + pbBStats.nHumanCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
					pbBStats.nElfCount = (pbBStats.nElfCount + pbBStats.nElfCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
					pbBStats.nDarkElfCount = (pbBStats.nDarkElfCount + pbBStats.nDarkElfCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
					pbBStats.nOrcCount = (pbBStats.nOrcCount + pbBStats.nOrcCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
					pbBStats.nDwarfCount = (pbBStats.nDwarfCount + pbBStats.nDwarfCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
					pbBStats.nKamaelCount = (pbBStats.nKamaelCount + pbBStats.nKamaelCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100) / 6;
				}
				else
				{
					pbBStats.nHumanCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
					pbBStats.nElfCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
					pbBStats.nDarkElfCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
					pbBStats.nOrcCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
					pbBStats.nDwarfCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
					pbBStats.nKamaelCount += Config.CBB_ONLINE_CHEAT_COUNT / 6;
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conAllPlayerCount, statementAllPlayerCount, rsAllPlayerCount);
		}
	}

	public void selectCurrentOnline()
	{
		pbBStats.nCurrentOnline = 0;
		pbBStats.nCurrentOfftrade = 0;
		int nOnlinePlayersCount = 0;
		int nOfftradePlayersCount = 0;

		nOnlinePlayersCount = GameObjectsStorage.getAllPlayersCount();

		if(!Config.SERVICES_OFFLINE_TRADE_ALLOW)
			nOfftradePlayersCount = 0;
		else
			nOfftradePlayersCount = GameObjectsStorage.getAllOfflineCount();

		if(Config.CBB_ONLINE_CHEAT_ENABLE)
			if(Config.CBB_ONLINE_CHEAT_PERCENT_ENABLE)
				pbBStats.nCurrentOnline = nOnlinePlayersCount - nOfftradePlayersCount + (nOnlinePlayersCount - nOfftradePlayersCount) * Config.CBB_ONLINE_CHEAT_PERCENT / 100;
			else
				pbBStats.nCurrentOnline = nOnlinePlayersCount - nOfftradePlayersCount + Config.CBB_ONLINE_CHEAT_COUNT;
		else
			pbBStats.nCurrentOnline = nOnlinePlayersCount - nOfftradePlayersCount;

		if(Config.CBB_OFFTRADE_CHEAT_ENABLE && Config.SERVICES_OFFLINE_TRADE_ALLOW)
			if(Config.CBB_OFFTRADE_CHEAT_PERCENT_ENABLE && Config.SERVICES_OFFLINE_TRADE_ALLOW)
				pbBStats.nCurrentOfftrade = nOfftradePlayersCount + nOfftradePlayersCount * Config.CBB_ONLINE_CHEAT_PERCENT / 100;
			else
				pbBStats.nCurrentOfftrade = nOfftradePlayersCount + Config.CBB_ONLINE_CHEAT_COUNT;
		else
			pbBStats.nCurrentOfftrade = nOfftradePlayersCount;
	}

	private void selectHeroCount()
	{
		Connection conHeroCount = null;
		PreparedStatement statementHeroCount = null;
		ResultSet rsHeroCount = null;

		pbBStats.nHeroCount = 0;

		try
		{
			conHeroCount = DatabaseFactory.getInstance().getConnection();
			statementHeroCount = conHeroCount.prepareStatement("SELECT * FROM heroes;");
			rsHeroCount = statementHeroCount.executeQuery();

			while(rsHeroCount.next())
				pbBStats.nHeroCount++;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conHeroCount, statementHeroCount, rsHeroCount);
		}
	}

	private void selectNobleCount()
	{
		Connection conNobleCount = null;
		PreparedStatement statementNobleCount = null;
		ResultSet rsNobleCount = null;

		pbBStats.nNobleCount = 0;

		try
		{
			conNobleCount = DatabaseFactory.getInstance().getConnection();
			statementNobleCount = conNobleCount.prepareStatement("SELECT * FROM olympiad_nobles;");
			rsNobleCount = statementNobleCount.executeQuery();

			while(rsNobleCount.next())
				pbBStats.nNobleCount++;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conNobleCount, statementNobleCount, rsNobleCount);
		}
	}
	
	private void showCastle(Player player) 
	{ 
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); 
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
	    try 
	    { 
	    	con = DatabaseFactory.getInstance().getConnection();
	    	statement = con.prepareStatement("SELECT * FROM castle ORDER BY id DESC LIMIT 10;"); 
	    	rs = statement.executeQuery(); 
	    	StringBuilder html = new StringBuilder();
	    	
	    	html.append("<table width=570>");
	    	String Owner = null; 
	    	String color = "FFFFFF";
	    	while (rs.next())
	    	{
	    		CBStatMan tp = new CBStatMan(); 
	    		tp.id = rs.getInt("id");
	    		tp.NameCastl = rs.getString("name");
	    		tp.Percent = (rs.getString("tax_percent") + "%"); 
	    		tp.siegeDate = sdf.format(new Date(rs.getLong("siege_date"))); 
	    		Owner = CastleManorManager.getInstance().getOwner(tp.id);

	    		if (Owner != null) 
	    		{ 
	    			color = "00CC00"; 
	    		} 
	    		else 
	    		{
	    			color = "FFFFFF"; 
	    			Owner = "Нет владельца"; 
	    		} 
	    	   	html.append("<tr>"); 
	    	   	html.append("<td width=150>" + tp.NameCastl + "</td>"); 
	    	   	html.append("<td width=100>" + tp.Percent + "</td>"); 
	    	   	html.append("<td width=200><font color=" + color + ">" + Owner + "</font></td>"); 
	    	   	html.append("<td width=150>" + tp.siegeDate + "</td>");
	    	   	html.append("</tr>"); 
	    	} 
	    	html.append("</table>"); 
	    	
	    	String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stat/stats_castle.htm", player);
			content = content.replace("%stats_castle%", html.toString());
			
	    	
	    	ShowBoard.separateAndSend(content, player);
	    	return;
	    }
	    catch (Exception e) 
		{ 
			e.printStackTrace(); 
		} 
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		} 
	} 
	private void showClan(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_subpledges.name,clan_data.clan_level,clan_data.reputation_score,clan_data.hasCastle,ally_data.ally_name FROM clan_data LEFT JOIN ally_data ON clan_data.ally_id = ally_data.ally_id LEFT JOIN `clan_subpledges` ON clan_data.clan_id = clan_subpledges.clan_id WHERE clan_data.clan_level>0 AND clan_subpledges.leader_id != '' order by clan_data.clan_level desc limit 10;");
			rs = statement.executeQuery();

			StringBuilder html = new StringBuilder();
			html.append("<table width=570>");
			while(rs.next())
			{
				CBStatMan tp = new CBStatMan();
				tp.ClanName = rs.getString("name");
				tp.AllyName = rs.getString("ally_name");
				tp.ReputationClan = rs.getInt("reputation_score");
				tp.ClanLevel = rs.getInt("clan_level");
				tp.hasCastle = rs.getInt("hasCastle");
				String hasCastle = "";
				String castleColor = "D70000";

				switch(tp.hasCastle)
				{
					case 1:
						hasCastle = "Глудио";
						castleColor = "00CC00";
						break;
					case 2:
						hasCastle = "Дион";
						castleColor = "00CC00";
						break;
					case 3:
						hasCastle = "Гиран";
						castleColor = "00CC00";
						break;
					case 4:
						hasCastle = "Орен";
						castleColor = "00CC00";
						break;
					case 5:
						hasCastle = "Аден";
						castleColor = "00CC00";
						break;
					case 6:
						hasCastle = "Хейн";
						castleColor = "00CC00";
						break;
					case 7:
						hasCastle = "Годдард";
						castleColor = "00CC00";
						break;
					case 8:
						hasCastle = "Руна";
						castleColor = "00CC00";
						break;
					case 9:
						hasCastle = "Шутгарт";
						castleColor = "00CC00";
						break;
					default:
						hasCastle = "Нету";
						castleColor = "D70000";
						break;
				}
				html.append("<tr>");
				html.append("<td width=150>" + tp.ClanName + "</td>");
				if(tp.AllyName != null)
					html.append("<td width=150>" + tp.AllyName + "</td>");
				else
					html.append("<td width=150>Нет альянса</td>");
				html.append("<td width=100>" + tp.ReputationClan + "</td>");
				html.append("<td width=50>" + tp.ClanLevel + "</td>");
				html.append("<td width=100><font color=" + castleColor + ">" + hasCastle + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stat/stats_clan.htm", player);
			content = content.replace("%stats_clan%", html.toString());
			ShowBoard.separateAndSend(content, player);
			return;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}
	private void selectClanCount()
	{
		Connection conClanCount = null;
		PreparedStatement statementClanCount = null;
		ResultSet rsClanCount = null;

		pbBStats.nClanCount = 0;

		try
		{
			conClanCount = DatabaseFactory.getInstance().getConnection();
			statementClanCount = conClanCount.prepareStatement("SELECT * FROM clan_data;");
			rsClanCount = statementClanCount.executeQuery();

			while(rsClanCount.next())
				pbBStats.nClanCount++;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conClanCount, statementClanCount, rsClanCount);
		}
	}

	private void selectAllyCount()
	{
		Connection conAllyCount = null;
		PreparedStatement statementAllyCount = null;
		ResultSet rsAllyCount = null;

		pbBStats.nAllyCount = 0;

		try
		{
			conAllyCount = DatabaseFactory.getInstance().getConnection();
			statementAllyCount = conAllyCount.prepareStatement("SELECT * FROM ally_data WHERE leader_id != '0';");
			rsAllyCount = statementAllyCount.executeQuery();

			while(rsAllyCount.next())
				pbBStats.nAllyCount++;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conAllyCount, statementAllyCount, rsAllyCount);
		}
	}

	public void selectTopPVP(Player player)
	{
		Connection conPVP = null;
		PreparedStatement statementPVP = null;
		ResultSet rsPVP = null;
		nCounter = 0;

		try
		{
			conPVP = DatabaseFactory.getInstance().getConnection();
			statementPVP = conPVP.prepareStatement("SELECT char_name, pvpkills, online FROM characters ORDER BY pvpkills DESC LIMIT 10;");
			rsPVP = statementPVP.executeQuery();

			while(rsPVP.next())
			{
				if(!rsPVP.getString("char_name").isEmpty())
				{
					pbBStats.asCharNameTopPvP[nCounter] = rsPVP.getString("char_name");
					pbBStats.asCharOnlineTopPvP[nCounter] = rsPVP.getInt("online");
					pbBStats.anCharPvPCount[nCounter] = rsPVP.getInt("pvpkills");
				}
				else
				{
					pbBStats.asCharNameTopPvP[nCounter] = player.isLangRus() ? "Нет" : "No";
					pbBStats.asCharOnlineTopPvP[nCounter] = 0;
					pbBStats.anCharPvPCount[nCounter] = 0;
				}
				nCounter++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conPVP, statementPVP, rsPVP);
		}

		return;
	}

	public void selectTopPK(Player player)
	{
		Connection conPK = null;
		PreparedStatement statementPK = null;
		ResultSet rsPK = null;
		nCounter = 0;

		try
		{
			conPK = DatabaseFactory.getInstance().getConnection();
			statementPK = conPK.prepareStatement("SELECT char_name, pkkills, online FROM characters ORDER BY pkkills DESC LIMIT 10;");
			rsPK = statementPK.executeQuery();
			while(rsPK.next())
			{
				if(!rsPK.getString("char_name").isEmpty())
				{
					pbBStats.asCharNameTopPk[nCounter] = rsPK.getString("char_name");
					pbBStats.asCharOnlineTopPk[nCounter] = rsPK.getInt("online");
					pbBStats.anCharPkCount[nCounter] = rsPK.getInt("pkkills");
				}
				else
				{
					pbBStats.asCharNameTopPk[nCounter] = player.isLangRus() ? "Нет" : "No";
					pbBStats.asCharOnlineTopPk[nCounter] = 0;
					pbBStats.anCharPkCount[nCounter] = 0;
				}
				nCounter++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conPK, statementPK, rsPK);
		}
	}

	public void selectTopRich(Player player)
	{
		Connection conRich = null;
		PreparedStatement statementRich = null;
		ResultSet rsRich = null;
		nCounter = 0;

		try
		{
			conRich = DatabaseFactory.getInstance().getConnection();
			statementRich = conRich.prepareStatement("SELECT i.owner_id, i.count, c.char_name FROM items i JOIN characters c ON c.obj_Id = i.owner_id WHERE i.item_id='57' ORDER BY i.count DESC LIMIT 0,10;");
			rsRich = statementRich.executeQuery();

			while(rsRich.next())
			{
				if(!rsRich.getString("i.owner_id").isEmpty())
				{
					pbBStats.asCharNameTopRich[nCounter] = rsRich.getString("char_name");
					pbBStats.anCharRichCount[nCounter] = rsRich.getLong("count");
				}
				else
				{
					pbBStats.asCharNameTopRich[nCounter] = player.isLangRus() ? "Нет данных" : "No data";
					pbBStats.anCharRichCount[nCounter] = 0;
				}
				nCounter++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conRich, statementRich, rsRich);
		}
	}

	public void selectTopClan(Player player)
	{
		Connection conClan = null;
		PreparedStatement statementClan = null;
		ResultSet rsClan = null;
		nCounter = 0;

		try
		{
			conClan = DatabaseFactory.getInstance().getConnection();
			statementClan = conClan.prepareStatement("SELECT cd.clan_level, cd.reputation_score, cs.name FROM clan_data cd JOIN clan_subpledges cs ON cs.clan_id=cd.clan_id WHERE cs.type='0' ORDER BY cd.clan_level DESC, cd.reputation_score DESC LIMIT 0,10;");
			rsClan = statementClan.executeQuery();

			while(rsClan.next())
			{
				if(!rsClan.getString("cs.name").isEmpty())
				{
					pbBStats.asCharNameTopClan[nCounter] = rsClan.getString("name");
					pbBStats.anCharClanCount[nCounter] = rsClan.getInt("reputation_score");
					pbBStats.anCharClanLevel[nCounter] = rsClan.getInt("clan_level");
				}
				else
				{
					pbBStats.asCharNameTopClan[nCounter] = "....";
					pbBStats.anCharClanCount[nCounter] = 0;
					pbBStats.anCharClanLevel[nCounter] = 0;
				}
				nCounter++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conClan, statementClan, rsClan);
		}
	}

	public void selectTopOnline(Player player)
	{
		Connection conOnline = null;
		PreparedStatement statementOnline = null;
		ResultSet rsOnline = null;
		nCounter = 0;

		try
		{
			conOnline = DatabaseFactory.getInstance().getConnection();
			statementOnline = conOnline.prepareStatement("SELECT char_name, onlinetime FROM characters ORDER BY onlinetime DESC LIMIT 10;");
			rsOnline = statementOnline.executeQuery();

			while(rsOnline.next())
			{
				if(!rsOnline.getString("char_name").isEmpty())
				{
					pbBStats.asCharNameTopOnline[nCounter] = rsOnline.getString("char_name");
					pbBStats.anCharOnlineTime[nCounter] = rsOnline.getInt("onlinetime");
				}
				else
				{
					pbBStats.asCharNameTopOnline[nCounter] = player.isLangRus() ? "Нет данных" : "No data";
					pbBStats.anCharOnlineTime[nCounter] = 0;
				}
				nCounter++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conOnline, statementOnline, rsOnline);
		}
	}

	public void epicRaidBossStatus()
	{
		Connection conRbStat = null;
		PreparedStatement statementRbStat = null;
		ResultSet rsRbStat = null;
		NpcTemplate npc;
		nCounter = 0;

		try
		{
			conRbStat = DatabaseFactory.getInstance().getConnection();
			statementRbStat = conRbStat.prepareStatement("SELECT id, respawn_delay FROM `raidboss_status` UNION SELECT bossId, respawnDate FROM `epic_boss_spawn`");
			rsRbStat = statementRbStat.executeQuery();

			while(rsRbStat.next())
			{
				for(int i = 0; i < Config.CBB_RB_LIST_STAT.length; i++)
				{
					if(Config.CBB_RB_LIST_STAT[i] == rsRbStat.getInt("id"))
					{
						npc = NpcHolder.getInstance().getTemplate(rsRbStat.getInt("id"));
						pbRBStats.asRaidBossName[nCounter] = npc.getName();
						pbRBStats.anRaidBossStatus[nCounter] = rsRbStat.getLong("respawn_delay") > 0 ? "Live" : "Dead";
						nCounter++;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conRbStat, statementRbStat, rsRbStat);
		}
	}

	String onlineTime(int time)
	{
		int onlinetimeD = 0;
		int onlinetimeH = 0;
		int onlinetimeM = 0;

		onlinetimeD = time / (24 * 3600);
		onlinetimeH = (time - onlinetimeD * 24 * 3600) / 3600;
		onlinetimeM = (time - onlinetimeD * 24 * 3600 - onlinetimeH * 3600) / 60;

		return "" + onlinetimeD + " д. " + onlinetimeH + " ч. " + onlinetimeM + " м.";
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}