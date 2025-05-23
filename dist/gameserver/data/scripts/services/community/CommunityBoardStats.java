package services.community;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.scripts.ScriptFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommunityBoardStats implements ScriptFile, ICommunityBoardHandler
{

    /**
     * Имплементированые методы скриптов
     */
    @Override
    public void onLoad() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public void onReload() {
        if (Config.COMMUNITYBOARD_ENABLED)
            CommunityBoardManager.getInstance().removeHandler(this);
    }

    @Override
    public void onShutdown() {
    }

    /**
     * Регистратор команд
     */
    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsstat", "_bbsstat_pk", "_bbsstat_online", "_bbsstat_clan", "_bbsstat_castle"};
    }

    /**
     * Класс общих пер-х
     */
    public class CBStatMan {
        public int PlayerId = 0; // obj_id Char
        public String ChName = ""; // Char name
        public int ChGameTime = 0; // Time in game
        public int ChPk = 0; // Char PK
        public int ChPvP = 0; // Char PVP
        public int ChOnOff = 0; // Char offline/online cure time
        public int ChSex = 0; // Char sex
        public String NameCastl;
        public Object siegeDate;
        public String Percent;
        public Object id2;
        public int id;
        public int ClanLevel;
        public int hasCastle;
        public int ReputationClan;
        public String AllyName;
        public String ClanName;
        public String Owner;
    }

    /**
     * Обработчик команд класса
     *
     * @param Player  - плеер (Call'er)
     * @param command - команда обработки
     */
    @Override
    public void onBypassCommand(Player player, String command) {
        if (command.equals("_bbsstat")) {
            showPvp(player);
            return;
        } else if (command.startsWith("_bbsstat_pk")) {
            showPK(player);
            return;
        } else if (command.startsWith("_bbsstat_online")) {
            showOnline(player);
            return;
        } else if (command.startsWith("_bbsstat_clan")) {
            showClan(player);
            return;
        } else if (command.startsWith("_bbsstat_castle")) {
            showCastle(player);
            return;
        } else
            ShowBoard.separateAndSend("<html><body><br><br><center>In community stats function: " + command + " not " +
		            "implemented yet</center><br><br></body></html>", player);
    }

    /**
     * Вызываем показ текущего списка лучших 20 плееров по ПВП показателю
     * Осуществляем внутри-классовый конект и чекинг таблицы (по приведённым параметрам)
     *
     * @param player
     */
    private void showPvp(Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pvpkills DESC LIMIT 20;");
            rs = statement.executeQuery();

            StringBuilder html = new StringBuilder();
            html.append("<table width=440>");
            while (rs.next()) {
                CBStatMan tp = new CBStatMan();
                tp.PlayerId = rs.getInt("obj_Id");
                tp.ChName = rs.getString("char_name");
                tp.ChSex = rs.getInt("sex");
                tp.ChGameTime = rs.getInt("onlinetime");
                tp.ChPk = rs.getInt("pkkills");
                tp.ChPvP = rs.getInt("pvpkills");
                tp.ChOnOff = rs.getInt("online");

                String sex = tp.ChSex == 1 ? "Ж" : "М";
                String color;
                String OnOff;
                if (tp.ChOnOff == 1) {
                    OnOff = "Online.";
                    color = "00CC00";
                } else {
                    OnOff = "Offline.";
                    color = "D70000";
                }
                html.append("<tr>");
                html.append("<td width=150 align=\"center\">" + tp.ChName + "</td>");
                html.append("<td width=50 align=\"center\">" + sex + "</td>");
                html.append("<td width=80 align=\"center\">" + OnlineTime(tp.ChGameTime) + "</td>");
                html.append("<td width=50 align=\"center\">" + tp.ChPk + "</td>");
                html.append("<td width=50 align=\"center\"><font color=00CC00>" + tp.ChPvP + "</font></td>");
                html.append("<td width=80 align=\"center\"><font color=" + color + ">" + OnOff + "</font></td>");
                html.append("</tr>");
            }
            html.append("</table>");

            String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stats/stats_top_pvp.htm", player);

            content = content.replace("%stats_top_pvp%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private void showPK(Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pkkills DESC LIMIT 20;");
            rs = statement.executeQuery();

            StringBuilder html = new StringBuilder();
            html.append("<table width=440>");
            while (rs.next()) {
                CBStatMan tp = new CBStatMan();
                tp.PlayerId = rs.getInt("obj_Id");
                tp.ChName = rs.getString("char_name");
                tp.ChSex = rs.getInt("sex");
                tp.ChGameTime = rs.getInt("onlinetime");
                tp.ChPk = rs.getInt("pkkills");
                tp.ChPvP = rs.getInt("pvpkills");
                tp.ChOnOff = rs.getInt("online");

                String sex = tp.ChSex == 1 ? "Ж" : "M";
                String color;
                String OnOff;
                if (tp.ChOnOff == 1) {
                    OnOff = "Online.";
                    color = "00CC00";
                } else {
                    OnOff = "Offline.";
                    color = "D70000";
                }
                html.append("<tr>");
                html.append("<td width=150 align=\"center\">" + tp.ChName + "</td>");
                html.append("<td width=50 align=\"center\">" + sex + "</td>");
                html.append("<td width=80 align=\"center\">" + OnlineTime(tp.ChGameTime) + "</td>");
                html.append("<td width=50 align=\"center\"><font color=00CC00>" + tp.ChPk + "</font></td>");
                html.append("<td width=50 align=\"center\">" + tp.ChPvP + "</td>");
                html.append("<td width=80 align=\"center\"><font color=" + color + ">" + OnOff + "</font></td>");
                html.append("</tr>");
            }
            html.append("</table>");
            String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stats/stats_top_pk.htm", player);
            content = content.replace("%stats_top_pk%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private void showOnline(Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY onlinetime DESC LIMIT 20;");
            rs = statement.executeQuery();

            StringBuilder html = new StringBuilder();
            html.append("<table width=440>");
            while (rs.next()) {
                CBStatMan tp = new CBStatMan();
                tp.PlayerId = rs.getInt("obj_Id");
                tp.ChName = rs.getString("char_name");
                tp.ChSex = rs.getInt("sex");
                tp.ChGameTime = rs.getInt("onlinetime");
                tp.ChPk = rs.getInt("pkkills");
                tp.ChPvP = rs.getInt("pvpkills");
                tp.ChOnOff = rs.getInt("online");

                String sex = tp.ChSex == 1 ? "Ж" : "М";
                String color;
                String OnOff;
                if (tp.ChOnOff == 1) {
                    OnOff = "Online.";
                    color = "00CC00";
                } else {
                    OnOff = "Offline.";
                    color = "D70000";
                }
                html.append("<tr>");
                html.append("<td width=150 align=\"center\">" + tp.ChName + "</td>");
                html.append("<td width=50 align=\"center\">" + sex + "</td>");
                html.append("<td width=80 align=\"center\"><font color=00CC00>" + OnlineTime(tp.ChGameTime) + "</font></td>");
                html.append("<td width=50 align=\"center\">" + tp.ChPk + "</td>");
                html.append("<td width=50 align=\"center\">" + tp.ChPvP + "</td>");
                html.append("<td width=80 align=\"center\"><font color=" + color + ">" + OnOff + "</font></td>");
                html.append("</tr>");
            }
            html.append("</table>");

            String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stats/stats_online.htm", player);
            content = content.replace("%stats_online%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private void showCastle(Player player) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT castle.name, castle.id, castle.tax_percent, castle.siege_date as siegeDate, clan_subpledges.name as clan_name, clan_data.clan_id " +
                    "FROM castle " +
                    "LEFT JOIN clan_data ON clan_data.hasCastle=castle.id " +
                    "LEFT JOIN clan_subpledges ON clan_subpledges.clan_id=clan_data.clan_id AND clan_subpledges.type='0';");
            rs = statement.executeQuery();
            StringBuilder html = new StringBuilder();
            html.append("<table width=460>");
            String color = "FFFFFF";

            while (rs.next()) {
                CBStatMan tp = new CBStatMan();
                tp.Owner = rs.getString("clan_name");
                tp.NameCastl = rs.getString("name");
                tp.Percent = rs.getString("tax_Percent") + "%";
                tp.siegeDate = sdf.format(new Date(rs.getLong("siegeDate")));

                if (tp.Owner != null)
                    color = "00CC00";
                else {
                    color = "FFFFFF";
                    tp.Owner = "Нет Владельца";
                }
                html.append("<tr>");
                html.append("<td width=160 align=\"center\">" + tp.NameCastl + "</td>");
                html.append("<td width=110 align=\"center\">" + tp.Percent + "</td>");
                html.append("<td width=200 align=\"center\"><font color=" + color + ">" + tp.Owner + "</font></td>");
                html.append("<td width=160 align=\"center\">" + tp.siegeDate + "</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stats/stats_castle.htm", player);
            content = content.replace("%stats_castle%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    private void showClan(Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT clan_subpledges.name,clan_data.clan_level,clan_data.reputation_score,clan_data.hasCastle,ally_data.ally_name FROM clan_data LEFT JOIN ally_data ON clan_data.ally_id = ally_data.ally_id, clan_subpledges WHERE clan_data.clan_level>0 AND clan_subpledges.clan_id=clan_data.clan_id AND clan_subpledges.type='0' order by clan_data.clan_level desc limit 20;");
            rs = statement.executeQuery();

            StringBuilder html = new StringBuilder();
            html.append("<table width=440>");
            while (rs.next()) {
                CBStatMan tp = new CBStatMan();
                tp.ClanName = rs.getString("name");
                tp.AllyName = rs.getString("ally_name");
                tp.ReputationClan = rs.getInt("reputation_score");
                tp.ClanLevel = rs.getInt("clan_level");
                tp.hasCastle = rs.getInt("hasCastle");
                String hasCastle = "";
                String castleColor = "D70000";

                switch (tp.hasCastle) {
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
                        hasCastle = "None";
                        castleColor = "D70000";
                        break;
                }
                html.append("<tr>");
                html.append("<td width=160>" + tp.ClanName + "</td>");
                if (tp.AllyName != null)
                    html.append("<td width=160>" + tp.AllyName + "</td>");
                else
                    html.append("<td width=160>Нет Альянса</td>");
                html.append("<td width=100 align=\"center\">" + tp.ReputationClan + "</td>");
                html.append("<td width=80 align=\"center\">" + tp.ClanLevel + "</td>");
                html.append("<td width=100 align=\"center\"><font color=" + castleColor + ">" + hasCastle + "</font></td>");
                html.append("</tr>");
            }
            html.append("</table>");
            String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/stats/stats_clan.htm", player);
            content = content.replace("%stats_clan%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    /**
     * Вызываем показ текущего списка лучших 20 плееров по PK показателю
     * Осуществляем внутри-классовый конект и чекинг таблицы (по приведённым параметрам)
     *
     * @param player
     */
    String OnlineTime(int time) 
    {
		int onlinetimeD = 0;
		int onlinetimeH = 0;
		int onlinetimeM = 0;

		onlinetimeD = time / (24 * 3600);
		onlinetimeH = (time - onlinetimeD * 24 * 3600) / 3600;
		onlinetimeM = (time - onlinetimeD * 24 * 3600 - onlinetimeH * 3600) / 60;

		return "" + onlinetimeD + " д. " + onlinetimeH + " ч. " + onlinetimeM + " м.";
    }

    /**
     * Не используемый, но вызываемый метод имплемента
     */
    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
    }
}