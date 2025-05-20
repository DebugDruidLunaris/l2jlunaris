package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.ChangeAllowedHwid;
import jts.gameserver.loginservercon.gspackets.ChangeAllowedIp;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

public class CommunityBoardSecurity
{
	public static String check(Player player, boolean ip_bind, boolean hwid_bind, boolean ip, boolean hwid)
	{
		String allow_hwid = "";
		String allow_ip = "";
		String result = "...";

		CustomMessage No = new CustomMessage("common.result.no", player);
		CustomMessage SecurityLock = new CustomMessage("communityboard.security.lock", player);
		CustomMessage SecurityUnlock = new CustomMessage("communityboard.security.unlock", player);
		CustomMessage Yes = new CustomMessage("common.result.yes", player);

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT allow_hwid, allow_ip FROM " + Config.LOGIN_DB + ".accounts WHERE login=? LIMIT 1");
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			if(rset.next())
			{
				allow_hwid = rset.getString("allow_hwid");
				allow_ip = rset.getString("allow_ip");
			}
		}
		catch(SQLException e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		boolean IP = allow_ip.equals("") || allow_ip.equals("NoGuard");
		boolean HWID = allow_hwid.equals("") || allow_hwid.equals("NoGuard");

		if(ip)
			result = IP ? "<font color=\"FF0000\">" + No + "</font>" : "<font color=\"18FF00\">" + Yes + "</font>";
		if(hwid)
			result = HWID ? "<font color=\"FF0000\">" + No + "</font>" : "<font color=\"18FF00\">" + Yes + "</font>";
		if(ip_bind)
			result = "<a action=\"bypass " + (IP ? "_bbssecurity:lockip\">" + SecurityLock + "" : "_bbssecurity:unlockip\">" + SecurityUnlock + "") + "</a>";
		if(hwid_bind)
			result = "<a action=\"bypass " + (HWID ? "_bbssecurity:lockhwid\">" + SecurityLock + "" : "_bbssecurity:unlockhwid\">" + SecurityUnlock + "") + "</a>";

		return result;
	}

	public static boolean lock(Player player, boolean ip, boolean hwid)
	{
		if(ip)
		{
			if(!Config.ALLOW_IP_LOCK)
			{
				String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_disable.htm", player);
				Functions.show(msg, player, null);
				return false;
			}

			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(player.getAccountName(), player.getIP()));
			String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_ip.htm", player);
			msg = msg.replace("%curIP%", player.getIP());
			Functions.show(msg, player, null);
			player.broadcastPacket(new MagicSkillUse(player, player, 5662, 1, 0, 0));
			return true;
		}

		if(hwid)
		{
			if(!Config.ALLOW_HWID_LOCK)
			{
				String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_disable.htm", player);
				Functions.show(msg, player, null);
				return false;
			}

			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(player.getAccountName(), player.getNetConnection().getHWID()));
			String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_hwid.htm", player);
			Functions.show(msg, player, null);
			player.broadcastPacket(new MagicSkillUse(player, player, 5662, 1, 1000, 0));
			return true;
		}
		return false;
	}

	public static boolean unlock(Player player, boolean ip, boolean hwid)
	{
		if(ip)
		{
			if(!Config.ALLOW_IP_LOCK)
			{
				String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_disable.htm", player);
				Functions.show(msg, player, null);
				return false;
			}

			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(player.getAccountName(), ""));
			String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/unlock_ip.htm", player);
			Functions.show(msg, player, null);
			player.broadcastPacket(new MagicSkillUse(player, player, 6802, 1, 1000, 0));
			return true;
		}

		if(hwid)
		{
			if(!Config.ALLOW_HWID_LOCK)
			{
				String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/lock_disable.htm", player);
				Functions.show(msg, player, null);
				return false;
			}

			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(player.getAccountName(), ""));
			String msg = HtmCache.getInstance().getNotNull("scripts/services/community/high/mods/lock/unlock_hwid.htm", player);
			Functions.show(msg, player, null);
			player.broadcastPacket(new MagicSkillUse(player, player, 6802, 1, 1000, 0));
			return true;
		}
		return false;
	}
}