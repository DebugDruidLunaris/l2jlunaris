package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.common.TeleportPoint;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.Zone.ZoneType;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;

public class CommunityBoardTeleport implements ScriptFile, ICommunityBoardHandler
{

	public class Teleport
	{
		public int TpId = 0; // Teport location ID
		public String TpName = ""; // Location name
		public int PlayerId = 0; // charID
		public int xC = 0; // Location coords X
		public int yC = 0; // Location coords Y
		public int zC = 0; // Location coords Z
	}

	private static final ZoneType[] FORBIDDEN_ZONES = new ZoneType[] 
	{
		ZoneType.RESIDENCE,
		ZoneType.ssq_zone,
		ZoneType.battle_zone,
		ZoneType.SIEGE,
		ZoneType.no_restart,
		ZoneType.no_summon, 
	};

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
			TeleportPoint.load();
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
		return new String[] { "_bbsteleport" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(bypass.equals("_bbsteleport"))
			showTeleportIndex(player);
		else if(bypass.startsWith("_bbsteleport:page"))
		{
			String[] b = bypass.split(" ");
			String page = b[1];
			ShowBoard.separateAndSend(HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/teleport/" + page + ".htm", player), player);
		}
		else if(bypass.equals("_bbsteleport:save_page"))
			showTeleportPoint(player);
		else if(bypass.startsWith("_bbsteleport:delete"))
		{
			StringTokenizer token = new StringTokenizer(bypass, " ");
			token.nextToken();
			int TpNameDell = Integer.parseInt(token.nextToken());
			deleteTeleportPoint(player, TpNameDell);
			showTeleportPoint(player);
		}
		else if(bypass.startsWith("_bbsteleport:save"))
		{
			String TpNameAdd = null;
			StringTokenizer token = new StringTokenizer(bypass, " ");
			token.nextToken();
			if(token.hasMoreTokens())
			{
				TpNameAdd = token.nextToken();
			}
			else
			{
				showTeleportPoint(player);
				return;
			}
			long AddTpPrice = Config.BBS_TELEPORT_SAVE_PRICE;
			addTeleportPoint(player, TpNameAdd, AddTpPrice);
			showTeleportPoint(player);
		}
		else if(bypass.startsWith("_bbsteleport:go"))
		{
			StringTokenizer token = new StringTokenizer(bypass, " ");
			token.nextToken();
			int xTp = Integer.parseInt(token.nextToken());
			int yTp = Integer.parseInt(token.nextToken());
			int zTp = Integer.parseInt(token.nextToken());
			goToTeleportPoint(player, xTp, yTp, zTp);
			showTeleportIndex(player);
		}
		else if(bypass.startsWith("_bbsteleport:id:"))
		{
			StringTokenizer token = new StringTokenizer(bypass, ":");
			token.nextToken();
			token.nextToken();
			int id = Integer.parseInt(token.nextToken());

			String name = TeleportPoint.teleport.get(id).getName();
			int priceId = TeleportPoint.teleport.get(id).getPriceId();
			int count = TeleportPoint.teleport.get(id).getPriceCount();
			int min = TeleportPoint.teleport.get(id).getMinLevel();
			int max = TeleportPoint.teleport.get(id).getMaxLevel();
			boolean pk = TeleportPoint.teleport.get(id).getPkAccess();
			boolean premium = TeleportPoint.teleport.get(id).getPremiumOnly();
			int premiumPriceId = TeleportPoint.teleport.get(id).getPremiumPriceId();
			int premiumCount = TeleportPoint.teleport.get(id).getPremiumPriceCount();
			int x = TeleportPoint.teleport.get(id).getX();
			int y = TeleportPoint.teleport.get(id).getY();
			int z = TeleportPoint.teleport.get(id).getZ();

			goToTeleportID(player, name, priceId, count, min, max, x, y, z, pk, premium, premiumPriceId, premiumCount);
			showTeleportIndex(player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(bypass) + "</center><br><br></body></html>", player);
		}
	}

	private void goToTeleportID(Player player, String name, int priceId, int count, int min, int max, int x, int y, int z, boolean pk, boolean premium, int premiumPriceId, int premiumCount)
	{
		Location loc = player.getLoc();
		int item;
		int price;
		int level = player.getLevel();

		if(level < min || level > max)
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.point.level.min.max", player).addNumber(min).addNumber(max));
			return;
		}

		if(pk && player.getKarma() > 0)
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.point.pk.denied", player));
			return;
		}

		if(premium && !player.hasBonus())
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.point.only.premium", player));
			return;
		}

		if(!checkFirstConditions(player) || !checkTeleportLocation(player, loc))
			return;

		if(player.hasBonus())
		{
			item = premiumPriceId;
			price = premiumCount;
		}
		else if(player.getLevel() > Config.BBS_TELEPORT_FREE_LEVEL)
		{
			item = priceId;
			price = count;
		}
		else
		{
			item = priceId;
			price = 0;
		}

		if(DifferentMethods.getPay(player, item, price, true))
		{
			player.teleToLocation(x, y, z);
			player.sendMessage(new CustomMessage("communityboard.teleport.point.success.location", player).addString(name));
		}
	}

	private void goToTeleportPoint(Player player, int xTp, int yTp, int zTp)
	{
		Location loc = player.getLoc();
		int priceTp;

		if(!checkFirstConditions(player) || !checkTeleportLocation(player, loc))
			return;

		if(player.hasBonus() && Config.BBS_TELEPORT_PRICE_PA)
			priceTp = 0;
		else if(player.getLevel() > Config.BBS_TELEPORT_FREE_LEVEL)
			priceTp = Config.BBS_TELEPORT_PRICE;
		else
			priceTp = 0;

		if(!DifferentMethods.getPay(player, Config.BBS_TELEPORT_ITEM_ID, priceTp, true))
			return;

		player.teleToLocation(xTp, yTp, zTp);
	}

	private void showTeleportPoint(Player player)
	{
		if(!player.hasBonus() && Config.BBS_TELEPORT_POINTS_PA)
			
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.personal.point.only.premium", player));
			DifferentMethods.communityNextPage(player, "_bbsteleport");
			return;
		}

		Teleport tp;
		StringBuilder html = new StringBuilder();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM bbs_teleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rset = statement.executeQuery();
			html.append("<table width=220>");

			while(rset.next())
			{
				tp = new Teleport();
				tp.TpId = rset.getInt("TpId");
				tp.TpName = rset.getString("name");
				tp.PlayerId = rset.getInt("charId");
				tp.xC = rset.getInt("xPos");
				tp.yC = rset.getInt("yPos");
				tp.zC = rset.getInt("zPos");

				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass _bbsteleport:go " + tp.xC + " " + tp.yC + " " + tp.zC + " " + 100000 + "\" width=200 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Удалить\" action=\"bypass _bbsteleport:delete " + tp.TpId + "\" width=80 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>)");
				html.append("</tr>");
			}

			html.append("</table>");

		}
		catch(SQLException e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/teleport/save.htm", player);
		content = content.replace("%tp%", html.toString());
		content = content.replace("<?tp_price?>", Integer.toString(Config.BBS_TELEPORT_PRICE));
		content = content.replace("<?tp_price_item?>", ItemHolder.getInstance().getTemplate(Config.BBS_TELEPORT_ITEM_ID).getName());
		content = content.replace("<?tp_price_item_save?>", ItemHolder.getInstance().getTemplate(Config.BBS_TELEPORT_SAVE_ITEM_ID).getName());
		content = content.replace("<?tp_max_count?>", Integer.toString(Config.BBS_TELEPORT_MAX_COUNT));
		content = content.replace("<?tp_free_min_lvl?>", Integer.toString(Config.BBS_TELEPORT_FREE_LEVEL));
		ShowBoard.separateAndSend(content, player);
		return;
	}

	private void showTeleportIndex(Player player)
	{
		String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/teleport/index.htm", player);
		content = content.replace("<?tp_price?>", Integer.toString(Config.BBS_TELEPORT_PRICE));
		content = content.replace("<?tp_price_item?>", ItemHolder.getInstance().getTemplate(Config.BBS_TELEPORT_ITEM_ID).getName());
		content = content.replace("<?tp_price_item_save?>", ItemHolder.getInstance().getTemplate(Config.BBS_TELEPORT_SAVE_ITEM_ID).getName());
		content = content.replace("<?tp_max_count?>", Integer.toString(Config.BBS_TELEPORT_MAX_COUNT));
		content = content.replace("<?tp_free_min_lvl?>", Integer.toString(Config.BBS_TELEPORT_FREE_LEVEL));
		ShowBoard.separateAndSend(content, player);
		return;
	}

	private void deleteTeleportPoint(Player player, int TpNameDell)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM bbs_teleport WHERE charId=? AND TpId=?;");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, TpNameDell);
			statement.execute();
		}
		catch(SQLException e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	@SuppressWarnings("resource")
	private void addTeleportPoint(Player player, String TpNameAdd, long AddTpPrice)
	{
		if(!DifferentMethods.getPay(player, Config.BBS_TELEPORT_SAVE_ITEM_ID, Config.BBS_TELEPORT_SAVE_PRICE, true))
			return;

		if(!checkFirstConditions(player))
			return;

		if(!player.hasBonus() && Config.BBS_TELEPORT_POINTS_PA)
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.personal.point.only.premium", player));
			return;
		}

		if(player.isMovementDisabled() || player.isOutOfControl())
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.persoanl.point.outofcontrol", player));
			return;
		}

		if(player.isInCombat())
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.persoanl.point.incombat", player));
			return;
		}

		if(player.isInZone(Zone.ZoneType.battle_zone) || player.isInZone(Zone.ZoneType.no_escape) || player.isInZone(Zone.ZoneType.epic) || player.isInZone(Zone.ZoneType.SIEGE) || player.isInZone(Zone.ZoneType.RESIDENCE) || player.getVar("jailed") != null)
		{
			player.sendMessage(new CustomMessage("communityboard.teleport.persoanl.point.forbidden.zone", player));
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT COUNT(*) FROM bbs_teleport WHERE charId=?;");
			statement.setLong(1, player.getObjectId());
			rset = statement.executeQuery();
			rset.next();

			if(rset.getInt(1) < Config.BBS_TELEPORT_MAX_COUNT)
			{
				statement = con.prepareStatement("SELECT COUNT(*) FROM bbs_teleport WHERE charId=? AND name=?;");
				statement.setLong(1, player.getObjectId());
				statement.setString(2, TpNameAdd);
				ResultSet rset1 = statement.executeQuery();
				rset1.next();

				statement = con.prepareStatement(rset1.getInt(1) == 0 ? "INSERT INTO bbs_teleport (charId, xPos, yPos, zPos, name) VALUES(?,?,?,?,?)" : "UPDATE bbs_teleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
				statement.setInt(1, player.getObjectId());
				statement.setInt(2, player.getX());
				statement.setInt(3, player.getY());
				statement.setInt(4, player.getZ());
				statement.setString(5, TpNameAdd);
				statement.execute();

			}
			else
				player.sendMessage(new CustomMessage("communityboard.teleport.personal.point.max", player).addNumber(Config.BBS_TELEPORT_MAX_COUNT));

		}
		catch(SQLException e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private static boolean checkFirstConditions(Player player)
	{
		if(player == null)
			return false;

		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}
		if(player.isInOlympiadMode())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		if(player.getReflection() != ReflectionManager.DEFAULT && !Config.BBS_TELEPORT_ALLOW_IN_INSTANCE)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return false;
		}
		if(player.isInDuel())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		if((player.isInCombat() || player.getPvpFlag() != 0) && !Config.BBS_TELEPORT_ALLOW_IN_COMBAT)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		if((player.isOnSiegeField() || player.isInZoneBattle()) && !Config.BBS_TELEPORT_ALLOW_ON_SIEGE)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGE_SCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE);
			return false;
		}
		if(player.isFlying())
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		if((player.isInWater() || player.isInBoat()) && !Config.BBS_TELEPORT_ALLOW_IN_UNDERWATHER)
		{
			player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}
		return true;
	}

	private static boolean checkTeleportLocation(Player player, Location loc)
	{
		return checkTeleportLocation(player, loc.x, loc.y, loc.z);
	}

	private static boolean checkTeleportLocation(Player player, int x, int y, int z)
	{
		if(player == null)
			return false;

		for(ZoneType zoneType : FORBIDDEN_ZONES)
		{
			Zone zone = player.getZone(zoneType);
			if(zone != null)
			{
				player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
				return false;
			}
		}
		return true;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}