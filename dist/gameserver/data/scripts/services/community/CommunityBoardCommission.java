package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.StringHolder;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.base.Element;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.network.serverpackets.ExNoticePostArrived;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.ItemTemplate.Grade;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;

public class CommunityBoardCommission extends Functions implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		if(Config.BBS_COMMISSION_ALLOW)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.BBS_COMMISSION_ALLOW)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbscommission" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(player == null)
			return;

		StringTokenizer st = new StringTokenizer(bypass, ":");
		st.nextToken();
		String action = st.hasMoreTokens() ? st.nextToken() : "main";

		if(action.equals("main"))
			showPage(player, 1, "all", "");
		else if(action.startsWith("list-"))
		{
			StringTokenizer list = new StringTokenizer(bypass, "-");
			list.nextToken();

			String category = list.hasMoreTokens() ? list.nextToken() : "all";
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			showPage(player, page, category, "");
		}
		else if(action.startsWith("search-"))
		{
			StringTokenizer list = new StringTokenizer(bypass, "-");
			list.nextToken();

			String search = list.hasMoreTokens() ? list.nextToken().trim() : "";
			search = strip(search);
			if(search.length() < 2)
			{
				player.sendMessage("Строка для поиска должна содержать не менее 2 символов");
				return;
			}
			int page = list.hasMoreTokens() ? Integer.parseInt(list.nextToken()) : 1;
			showPage(player, page, "search", search);
		}
		else if(action.startsWith("show-"))
		{
			StringTokenizer stid = new StringTokenizer(bypass, "-");
			stid.nextToken();

			int id;
			try
			{
				id = Integer.parseInt(stid.nextToken());
			}
			catch(Exception e)
			{
				return;
			}
			showItem(player, id);
		}
		else if(action.startsWith("add-"))
		{
			StringTokenizer stid = new StringTokenizer(bypass, "-");
			stid.nextToken();

			int id;
			try
			{
				id = Integer.parseInt(stid.nextToken());
			}
			catch(Exception e)
			{
				return;
			}
			addProduct(player, id);
		}
		else if(action.startsWith("select-"))
		{
			StringTokenizer stid = new StringTokenizer(bypass, "-");
			stid.nextToken();

			int objId;
			try
			{
				objId = Integer.parseInt(stid.nextToken());
			}
			catch(Exception e)
			{
				return;
			}
			selectProduct(player, objId);
		}
		else if(action.startsWith("create-"))
		{
			StringTokenizer stid = new StringTokenizer(bypass, "-");
			stid.nextToken();

			int objId;
			String price;
			int price_id;
			long price_count;
			long item_count;
			try
			{
				objId = Integer.parseInt(stid.nextToken());
				price = stid.nextToken().trim();
				price_count = Long.parseLong(stid.nextToken().trim());
				item_count = Integer.parseInt(stid.nextToken().trim());
			}
			catch(Exception e)
			{
				return;
			}
			price_id = getPriceId(price);
			if(price_id == 0)
				return;
			createProduct(player, objId, price_id, price_count, item_count);
		}
		else if(action.startsWith("get-"))
		{
			StringTokenizer stid = new StringTokenizer(bypass, "-");
			stid.nextToken();

			int id;
			try
			{
				id = Integer.parseInt(stid.nextToken());
			}
			catch(Exception e)
			{
				return;
			}
			getItem(player, id);
			showPage(player, 1, "all", "");
		}
	}

	private void createProduct(Player player, int objId, int price_id, long price_count, long item_count)
	{
		ItemInstance item = player.getInventory().getItemByObjectId(objId);
		if(item == null || !checkItem(item))
			return;

		if(item.getCount() < item_count && item.getCount() - item_count < 0)
		{
			player.sendMessage(("Максимальная сумма, которая может быть выставлена на продажу равна ") + item.getCount());
			return;
		}

		int need_item_id = 0;
		int need_item_count = 0;
		if(item.isArmor())
		{
			need_item_id = Config.BBS_COMMISSION_ARMOR_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_ARMOR_PRICE[1];
		}
		else if(item.isWeapon())
		{
			need_item_id = Config.BBS_COMMISSION_WEAPON_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_WEAPON_PRICE[1];
		}
		else if(item.isAccessory())
		{
			need_item_id = Config.BBS_COMMISSION_JEWERLY_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_JEWERLY_PRICE[1];
		}
		else
		{
			need_item_id = Config.BBS_COMMISSION_OTHER_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_OTHER_PRICE[1];
		}

		if(need_item_id > 0 && need_item_count > 0)
		{
			if(!DifferentMethods.getPay(player, need_item_id, need_item_count, true))
				return;
			else
				Functions.removeItem(player, need_item_id, need_item_count);
		}

		int item_id = item.getItemId();
		int enchant_level = item.getEnchantLevel();
		int augment_id = item.getAugmentationId();
		int attribute_fire = item.getAttributes().getFire();
		int attribute_water = item.getAttributes().getWater();
		int attribute_wind = item.getAttributes().getWind();
		int attribute_earth = item.getAttributes().getEarth();
		int attribute_holy = item.getAttributes().getHoly();
		int attribute_unholy = item.getAttributes().getUnholy();
		String category = "";
		if(item.isWeapon())
			category = "weapon";
		else if(item.isArmor())
			category = "armor";
		else if(item.isAccessory())
			category = "jewelry";
		else if(item.getTemplate().getItemClass() == ItemTemplate.ItemClass.MATHERIALS)
			category = "matherials";
		else if(item.getTemplate().getItemClass() == ItemTemplate.ItemClass.PIECES)
			category = "pieces";
		else if(item.getTemplate().getItemClass() == ItemTemplate.ItemClass.RECIPIES)
			category = "recipe";
		else
			category = "other";
		long endTime = Config.BBS_COMMISSION_HIDE_OLD_AFTER * 24 * 60 * 60 * 1000;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO `bbs_commission` (`owner_id`, `category`, `item_id`, `item_name`, `item_count`, `enchant_level`, `augment_id`, `attribute_fire`, `attribute_water`, `attribute_wind`, `attribute_earth`, `attribute_holy`, `attribute_unholy`, `price_id`, `price_count`, `date`, `endtime`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, category);
			statement.setInt(3, item_id);
			statement.setString(4, item.getName());
			statement.setLong(5, item_count);
			statement.setInt(6, enchant_level);
			statement.setInt(7, augment_id);
			statement.setInt(8, attribute_fire);
			statement.setInt(9, attribute_water);
			statement.setInt(10, attribute_wind);
			statement.setInt(11, attribute_earth);
			statement.setInt(12, attribute_holy);
			statement.setInt(13, attribute_unholy);
			statement.setInt(14, price_id);
			statement.setLong(15, price_count);
			statement.setLong(16, System.currentTimeMillis());
			statement.setLong(17, System.currentTimeMillis() + endTime);
			statement.execute();

			if(!DifferentMethods.getPay(player, item.getItemId(), item_count, false))

			Log.add("put up for sale " + item_count + " " + DifferentMethods.getItemName(item_id) + " [ID: " + item_id + ", UniqId: " + objId + "].", "CommunityBoardCommission: ", player);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		showPage(player, 1, "myitems", "");
	}

	private void selectProduct(Player player, int objId)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/commission/index.htm", player);
		ItemInstance item = player.getInventory().getItemByObjectId(objId);
		if(item == null || !checkItem(item))
			return;

		StringBuilder sb = new StringBuilder();
		int enchant_level = item.getEnchantLevel();
		int augment_id = item.getAugmentationId();
		int attribute_fire = item.getAttributes().getFire();
		int attribute_water = item.getAttributes().getWater();
		int attribute_wind = item.getAttributes().getWind();
		int attribute_earth = item.getAttributes().getEarth();
		int attribute_holy = item.getAttributes().getHoly();
		int attribute_unholy = item.getAttributes().getUnholy();
		int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
		long item_count = item.getCount();

		String icon = "l2ui_ch3.petitem_click";
		if(item.getTemplate().isPvP())
			icon = "icon.pvp_tab";
		else if(item.getItemId() == 9912)
			icon = "icon.fort_tab";

		sb.append("<table width=594 height=40 border=0>");
		sb.append("<tr>");
		sb.append("<td width=30 height=32 align=center>");
		sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getTemplate().getIcon()).append("\">");
		sb.append("<tr>");
		sb.append("<td valign=top align=center>");
		sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("<td width=400 align=left>");
		sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getTemplate().getAdditionalName().length() > 0 ? "-&nbsp;" + item.getTemplate().getAdditionalName() : "").append("</font> ").append(enchant_level > 0 ? "+" + enchant_level : "");
		sb.append("<br1>");
		sb.append(new CustomMessage("communityboard.commission.shop.sell.count", player).addString(Util.formatAdena(item_count)).addString(DifferentMethods.declension(player, (int) item_count, "Piece")).toString());
		sb.append("</td>");
		sb.append("<td width=160 align=right>");
		sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
		sb.append("<tr>");
		if(item.getTemplate().isPvP())
		{
			sb.append("<td width=36 height=32 align=right>");
			sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
			sb.append("<tr>");
			sb.append("<td valign=top align=center>");
			sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("</td>");
			sb.append("<td width=6 height=32 align=right>");
			sb.append("</td>");
		}
		if(enchant_level > 0)
		{
			sb.append("<td width=36 height=32 align=right>");
			sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
			sb.append("<tr>");
			sb.append("<td valign=top align=center>");
			sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("</td>");
			sb.append("<td width=6 height=32 align=right>");
			sb.append("</td>");
		}
		if(att_count > 0)
		{
			sb.append("<td width=36 height=32 align=right>");
			sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
			sb.append("<tr>");
			sb.append("<td valign=top align=center>");
			sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("</td>");
			sb.append("<td width=6 height=32 align=right>");
			sb.append("</td>");
		}
		if(augment_id > 0)
		{
			sb.append("<td width=36 height=32 align=right>");
			sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
			sb.append("<tr>");
			sb.append("<td valign=top align=center>");
			sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("</td>");
			sb.append("<td width=6 height=32 align=right>");
			sb.append("</td>");
		}
		sb.append("<td width=36 height=32 align=right>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<table width=594 border=0>");
		sb.append("<tr>");
		sb.append("<td width=594 height=10>");
		sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");

		if(att_count > 0)
		{
			if(attribute_fire > 0)
			{
				String icon_fire = "icon.etc_fire_stone_i00";
				if(item.isWeapon() && attribute_fire > 150 || item.isArmor() && attribute_fire > 60)
				{
					icon_fire = "icon.etc_fire_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_fire).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=ef1c29>").append(new CustomMessage("common.element.0", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=ef1c29>").append(attribute_fire).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			if(attribute_water > 0)
			{
				String icon_water = "icon.etc_water_stone_i00";
				if(item.isWeapon() && attribute_water > 150 || item.isArmor() && attribute_water > 60)
				{
					icon_water = "icon.etc_water_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_water).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=1896de>").append(new CustomMessage("common.element.1", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=1896de>").append(attribute_water).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			if(attribute_wind > 0)
			{
				String icon_wind = "icon.etc_wind_stone_i00";
				if(item.isWeapon() && attribute_wind > 150 || item.isArmor() && attribute_wind > 60)
				{
					icon_wind = "icon.etc_wind_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_wind).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=5ababd>").append(new CustomMessage("common.element.2", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=5ababd>").append(attribute_wind).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			if(attribute_earth > 0)
			{
				String icon_earth = "icon.etc_earth_stone_i00";
				if(item.isWeapon() && attribute_earth > 150 || item.isArmor() && attribute_earth > 60)
				{
					icon_earth = "icon.etc_earth_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_earth).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=347021>").append(new CustomMessage("common.element.3", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=347021>").append(attribute_earth).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			if(attribute_holy > 0)
			{
				String icon_holy = "icon.etc_holy_stone_i00";
				if(item.isWeapon() && attribute_holy > 150 || item.isArmor() && attribute_holy > 60)
				{
					icon_holy = "icon.etc_holy_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_holy).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=52d3ff>").append(new CustomMessage("common.element.4", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=52d3ff>").append(attribute_holy).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}

			if(attribute_unholy > 0)
			{
				String icon_unholy = "icon.etc_unholy_stone_i00";
				if(item.isWeapon() && attribute_unholy > 150 || item.isArmor() && attribute_unholy > 60)
				{
					icon_unholy = "icon.etc_unholy_crystal_i00";
				}
				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_unholy).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=632970>").append(new CustomMessage("common.element.5", player).toString()).append("</font>");
				sb.append("<br1>");
				sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=632970>").append(attribute_unholy).append("</font>");
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}
		}

		sb.append("<table width=594 border=0>");
		if(item.isStackable() && item.getCount() > 1)
		{
			sb.append("<tr>");
			sb.append("<td width=594 align=center>");
			sb.append(new CustomMessage("communityboard.commission.shop.set.count", player).addString(Util.formatAdena(item_count)).addString(DifferentMethods.declension(player, (int) item_count, "Piece")).toString());
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("	<td width=594 align=center>");
			sb.append("<br><edit var=\"item_count\" width=200 height=10><br>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td width=594 height=10>");
			sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
			sb.append("</td>");
			sb.append("</tr>");
		}
		sb.append("<tr>");
		sb.append("<td width=594 align=center>");
		sb.append(new CustomMessage("communityboard.commission.shop.info", player).toString());
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td align=center>");
		sb.append("<table width=500 border=0 cellspacing=1 cellpadding=1>");
		sb.append("<tr>");
		sb.append("<td width=270 align=center>");
		sb.append("<edit var=\"price_coint\" width=100 height=10>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td width=270 align=center>");
		sb.append("<combobox var=\"price\" list=\"").append(getAvailablePrice()).append("\" width=100 height=15><br><br>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td width=270 align=center>");
		sb.append("<button action=\"bypass _bbscommission:create-").append(objId).append("- $price - $price_coint - ").append(item.isStackable() && item.getCount() > 1 ? " $item_count " : "1").append("\" value=\"").append(new CustomMessage("communityboard.commission.shop.sell", player).toString()).append("\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_BuyEtc_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_BuyEtc\">");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");

		int need_item_id = 0;
		int need_item_count = 0;
		if(item.isArmor())
		{
			need_item_id = Config.BBS_COMMISSION_ARMOR_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_ARMOR_PRICE[1];
		}
		else if(item.isWeapon())
		{
			need_item_id = Config.BBS_COMMISSION_WEAPON_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_WEAPON_PRICE[1];
		}
		else if(item.isAccessory())
		{
			need_item_id = Config.BBS_COMMISSION_JEWERLY_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_JEWERLY_PRICE[1];
		}
		else
		{
			need_item_id = Config.BBS_COMMISSION_OTHER_PRICE[0];
			need_item_count = Config.BBS_COMMISSION_OTHER_PRICE[1];
		}

		if(need_item_id > 0 && need_item_count > 0)
		{
			sb.append("<tr>");
			sb.append("<td width=594 align=center>");
			sb.append("<br>").append(new CustomMessage("communityboard.commission.shop.attention", player).addNumber(need_item_count).addItemName(need_item_id).toString()).append("");
			sb.append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");

		html = html.replace("<?content?>", sb.toString());
		html = html.replace("<?pages?>", "");
		html = html.replace("<?category?>", "select");
		html = html.replace("<?category_name?>", getCategoryName(player, "select"));
		ShowBoard.separateAndSend(html, player);
	}

	public String getAvailablePrice()
	{
		String rewards = "";
		for(int id : Config.BBS_COMMISSION_ITEMS)
			if(rewards.isEmpty())
				rewards += DifferentMethods.getItemName(id);
			else
				rewards += ";" + DifferentMethods.getItemName(id);
		return rewards;
	}

	public int getPriceId(String name)
	{
		for(int id : Config.BBS_COMMISSION_ITEMS)
			if(DifferentMethods.getItemName(id).equalsIgnoreCase(name) && name.length() > 1)
				return id;

		return 0;
	}

	private void addProduct(Player player, int page)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/commission/index.htm", player);

		List<ItemInstance> temp = new ArrayList<ItemInstance>();
		for(ItemInstance item : player.getInventory().getItems())
		{
			if(!checkItem(item))
				continue;

			temp.add(item);
		}

		if(temp.size() <= Config.BBS_COMMISSION_COUNT_TO_PAGE)
			page = 1;

		ItemInstance item;

		StringBuilder sb = new StringBuilder();
		int coun = 2;
		for(int i = page * Config.BBS_COMMISSION_COUNT_TO_PAGE - Config.BBS_COMMISSION_COUNT_TO_PAGE; i < page * Config.BBS_COMMISSION_COUNT_TO_PAGE; i++)
		{
			if(i >= temp.size())
				break;

			item = temp.get(i);

			if(item != null)
			{
				String icon = "l2ui_ch3.petitem_click";
				if(item.getTemplate().isPvP())
					icon = "icon.pvp_tab";
				else if(item.getItemId() == 9912)
					icon = "icon.fort_tab";

				if(coun == 1)
					coun = 2;
				else
					coun = 1;
				int objId = item.getObjectId();
				if(item.isWeapon())
				{
					int enchant_level = item.getEnchantLevel();
					int augment_id = item.getAugmentationId();
					int attribute_fire = item.getAttributes().getFire();
					int attribute_water = item.getAttributes().getWater();
					int attribute_wind = item.getAttributes().getWind();
					int attribute_earth = item.getAttributes().getEarth();
					int attribute_holy = item.getAttributes().getHoly();
					int attribute_unholy = item.getAttributes().getUnholy();
					int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getTemplate().getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getTemplate().getAdditionalName().length() > 0 ? "-&nbsp;" + item.getTemplate().getAdditionalName() : "").append("</font> (").append(item.getCrystalType().toString()).append(")<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.count", player).addNumber(1).addString(DifferentMethods.declension(player, 1, "Piece")).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.getTemplate().isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(att_count > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(augment_id > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:select-").append(objId).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else if(item.isArmor())
				{
					int enchant_level = item.getEnchantLevel();
					int attribute_fire = item.getAttributes().getFire();
					int attribute_water = item.getAttributes().getWater();
					int attribute_wind = item.getAttributes().getWind();
					int attribute_earth = item.getAttributes().getEarth();
					int attribute_holy = item.getAttributes().getHoly();
					int attribute_unholy = item.getAttributes().getUnholy();
					int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getTemplate().getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getTemplate().getAdditionalName().length() > 0 ? "-&nbsp;" + item.getTemplate().getAdditionalName() : "").append("</font> (").append(item.getCrystalType().toString()).append(")<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.count", player).addNumber(1).addString(DifferentMethods.declension(player, 1, "Piece")).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.getTemplate().isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(att_count > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:select-").append(objId).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else if(item.isAccessory())
				{
					int enchant_level = item.getEnchantLevel();
					int augment_id = item.getAugmentationId();

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getTemplate().getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getTemplate().getAdditionalName().length() > 0 ? "-&nbsp;" + item.getTemplate().getAdditionalName() : "").append("</font> (").append(item.getCrystalType().toString()).append(")<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.count", player).addNumber(1).addString(DifferentMethods.declension(player, 1, "Piece")).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.getTemplate().isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(augment_id > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:select-").append(objId).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else
				{
					long item_count = item.getCount();

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getTemplate().getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.count", player).addString(Util.formatAdena(item_count)).addString(DifferentMethods.declension(player, (int) item_count, "Piece")).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=150 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:select-").append(objId).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
			}
		}
		StringBuilder pg = new StringBuilder();

		double items = temp.size();
		double MaxItemPerPage = Config.BBS_COMMISSION_COUNT_TO_PAGE;

		if(items > MaxItemPerPage)
		{
			double MaxPages = Math.ceil(items / MaxItemPerPage);

			pg.append("<center><table width=25 border=0><tr>");
			int ButtonInLine = 1;

			for(int current = 1; current <= MaxPages; current++)
			{
				if(page == current)
				{
					pg.append("<td width=25 align=center><button value=\"[").append(current).append("]\" width=28 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					pg.append("<td width=25 align=center><button value=\"").append(current).append("\" action=\"bypass _bbscommission:add-").append(current).append("\" width=28 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				if(ButtonInLine == 18)
				{
					pg.append("</tr><tr>");
					ButtonInLine = 0;
				}
				ButtonInLine++;
			}
			pg.append("</tr></table></center>");
		}
		html = html.replace("<?content?>", sb.toString());
		html = html.replace("<?pages?>", pg.toString());
		html = html.replace("<?category?>", "add");
		html = html.replace("<?category_name?>", getCategoryName(player, "add"));
		ShowBoard.separateAndSend(html, player);
	}

	private void showItem(Player player, int id)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/commission/index.htm", player);

		StringBuilder sb = new StringBuilder();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `id` = ?");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int item_id = rset.getInt("item_id");
				long item_count = rset.getLong("item_count");
				int owner_id = rset.getInt("owner_id");
				int price_id = rset.getInt("price_id");
				long price_count = rset.getLong("price_count");
				if(price_id == 0 || item_id == 0)
					continue;
				int enchant_level = rset.getInt("enchant_level");
				int augment_id = rset.getInt("augment_id");
				int attribute_fire = rset.getInt("attribute_fire");
				int attribute_water = rset.getInt("attribute_water");
				int attribute_wind = rset.getInt("attribute_wind");
				int attribute_earth = rset.getInt("attribute_earth");
				int attribute_holy = rset.getInt("attribute_holy");
				int attribute_unholy = rset.getInt("attribute_unholy");
				long date = rset.getLong("endtime");
				int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
				ItemTemplate item = ItemHolder.getInstance().getTemplate(item_id);
				if(item == null)
					continue;

				String icon = "l2ui_ch3.petitem_click";
				if(item.isPvP())
					icon = "icon.pvp_tab";
				else if(item.getItemId() == 9912)
					icon = "icon.fort_tab";

				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getIcon()).append("\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getAdditionalName().length() > 0 ? "-&nbsp;" + item.getAdditionalName() : "").append("</font> ").append(item.getCrystalType() != Grade.NONE ? "(" + item.getCrystalType().toString() + ")" : "").append("<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
				sb.append("<br1>");
				sb.append(new CustomMessage("communityboard.commission.shop.price", player).addString(Util.formatAdena(price_count)).addString(" " + DifferentMethods.getItemName(price_id)).toString());
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				if(item.isPvP())
				{
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=6 height=32 align=right>");
					sb.append("</td>");
				}
				if(enchant_level > 0)
				{
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=6 height=32 align=right>");
					sb.append("</td>");
				}
				if(att_count > 0)
				{
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=6 height=32 align=right>");
					sb.append("</td>");
				}
				if(augment_id > 0)
				{
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=6 height=32 align=right>");
					sb.append("</td>");
				}
				if(owner_id != player.getObjectId())
				{
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:get-").append(id).append("\" width=32 height=32 back=\"l2ui_ch3.PremiumItemBtn_Down\" fore=\"l2ui_ch3.PremiumItemBtn\">");
					sb.append("</td>");
				}
				else
				{

					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:get-").append(id).append("\" width=32 height=32 back=\"l2ui_ct1.Icon_df_Min_MacroEdit_Down\" fore=\"l2ui_ct1.Icon_df_Min_MacroEdit\">");
					sb.append("</td>");
				}
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");

				if(att_count > 0)
				{
					if(attribute_fire > 0)
					{
						String icon_fire = "icon.etc_fire_stone_i00";
						if(item.isWeapon() && attribute_fire > 150 || item.isArmor() && attribute_fire > 60)
						{
							icon_fire = "icon.etc_fire_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_fire).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=ef1c29>").append(new CustomMessage("common.element.0", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=ef1c29>").append(attribute_fire).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}

					if(attribute_water > 0)
					{
						String icon_water = "icon.etc_water_stone_i00";
						if(item.isWeapon() && attribute_water > 150 || item.isArmor() && attribute_water > 60)
						{
							icon_water = "icon.etc_water_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_water).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=1896de>").append(new CustomMessage("common.element.1", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=1896de>").append(attribute_water).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}

					if(attribute_wind > 0)
					{
						String icon_wind = "icon.etc_wind_stone_i00";
						if(item.isWeapon() && attribute_wind > 150 || item.isArmor() && attribute_wind > 60)
						{
							icon_wind = "icon.etc_wind_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_wind).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=5ababd>").append(new CustomMessage("common.element.2", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=5ababd>").append(attribute_wind).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}

					if(attribute_earth > 0)
					{
						String icon_earth = "icon.etc_earth_stone_i00";
						if(item.isWeapon() && attribute_earth > 150 || item.isArmor() && attribute_earth > 60)
						{
							icon_earth = "icon.etc_earth_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_earth).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=347021>").append(new CustomMessage("common.element.3", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=347021>").append(attribute_earth).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}

					if(attribute_holy > 0)
					{
						String icon_holy = "icon.etc_holy_stone_i00";
						if(item.isWeapon() && attribute_holy > 150 || item.isArmor() && attribute_holy > 60)
						{
							icon_holy = "icon.etc_holy_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_holy).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=52d3ff>").append(new CustomMessage("common.element.4", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=52d3ff>").append(attribute_holy).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}

					if(attribute_unholy > 0)
					{
						String icon_unholy = "icon.etc_unholy_stone_i00";
						if(item.isWeapon() && attribute_unholy > 150 || item.isArmor() && attribute_unholy > 60)
						{
							icon_unholy = "icon.etc_unholy_crystal_i00";
						}
						sb.append("<table width=594 height=40 border=0>");
						sb.append("<tr>");
						sb.append("<td width=30 height=32 align=center>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(icon_unholy).append("\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=400 align=left>");
						sb.append("<font color=632970>").append(new CustomMessage("common.element.5", player).toString()).append("</font>");
						sb.append("<br1>");
						sb.append("<font color=B59A75>").append(new CustomMessage("common.element.bonus", player).toString()).append("</font> <font color=632970>").append(attribute_unholy).append("</font>");
						sb.append("</td>");
						sb.append("<td width=160 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
						sb.append("<tr>");
						sb.append("<td width=36 height=32 align=right>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("<table width=594 border=0>");
						sb.append("<tr>");
						sb.append("<td width=594 height=10>");
						sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
					}
				}

				if(item.isStackable() && item_count > 1)
				{
					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mechanic_box_i00\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append(new CustomMessage("communityboard.commission.shop.sell.info", player).toString());
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.sell.count", player).addString(Util.formatAdena(item_count)).addString(DifferentMethods.declension(player, (int) item_count, "Piece")).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					sb.append("<td width=36 height=32 align=right>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}

				if(Config.BBS_COMMISSION_HIDE_OLD_ITEMS)
				{
					int Day, Hour = 0;
					int time = (int) (date - System.currentTimeMillis());
					int endtime = time / 1000;
					Day = Math.round(endtime / 60 / 60 / 24);
					Hour = (endtime - Day * 24 * 60 * 60) / 60 / 60;
					String _endTime = null;
					if(Day > 1)
						_endTime = Day + " " + DifferentMethods.declension(player, Day, "Days");
					else if(Day < 1 && Hour >= 1)
						_endTime = Hour + " " + DifferentMethods.declension(player, Hour, "Hour");
					else
						_endTime = new CustomMessage("communityboard.commission.shop.sell.isend", player).toString();

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill5239\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append(new CustomMessage("communityboard.commission.shop.sell.end", player).toString());
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.sell.endtime", player).addString(_endTime).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					sb.append("<td width=36 height=32 align=right>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}

				sb.append("<table width=594 height=40 border=0>");
				sb.append("<tr>");
				sb.append("<td width=30 height=32 align=center>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill5243\">");
				sb.append("<tr>");
				sb.append("<td valign=top align=center>");
				sb.append("<img src=\"icon.panel_2\" width=32 height=32>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("<td width=400 align=left>");
				sb.append(new CustomMessage("communityboard.commission.shop.seller", player).toString());
				sb.append("<br1>");
				sb.append(new CustomMessage("communityboard.commission.shop.seller.name", player).addString(owner_id == player.getObjectId() ? "вы" : getCharName(owner_id)).toString());
				sb.append("</td>");
				sb.append("<td width=160 align=right>");
				sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
				sb.append("<tr>");
				sb.append("<td width=36 height=32 align=right>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
				sb.append("<table width=594 border=0>");
				sb.append("<tr>");
				sb.append("<td width=594 height=10>");
				sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
				sb.append("</td>");
				sb.append("</tr>");
				sb.append("</table>");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		html = html.replace("<?content?>", sb.toString());
		html = html.replace("<?category_name?>", new CustomMessage("communityboard.commission.category.review", player).toString());
		html = html.replace("<?pages?>", "");
		ShowBoard.separateAndSend(html, player);
	}

	private String getCharName(int objId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `obj_Id` = ? LIMIT 1");
			statement.setInt(1, objId);
			rset = statement.executeQuery();
			if(rset.next())
				return rset.getString("char_name");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return "No char";
	}

	private void showPage(Player player, int page, String category, String search)
	{
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/commission/index.htm", player);
		String content = getContent(player, page, category, search, player.getObjectId());
		if(content.length() < 10)
			if(search.isEmpty() || search.length() < 2)
				content = "<table width=594 border=0><tr><td width=594 height=38 align=center><br>" + (new CustomMessage("communityboard.commission.shop.list.empty", player).toString()) + "</td></tr></table><br>";
			else if(category.equalsIgnoreCase("myitems"))
				content = "<table width=594 border=0><tr><td width=594 height=38 align=center><br>" + (new CustomMessage("communityboard.commission.shop.inventory.empty", player).toString()) + "</td></tr></table><br>";
			else
				content = "<table width=594 border=0><tr><td width=594 height=38 align=center><br>" + (new CustomMessage("communityboard.commission.shop.search.empty", player).addString(search).toString()) + "</td></tr></table><br>";

		StringBuilder pg = new StringBuilder();

		double items = getItemsCount(category, search, player.getObjectId());
		double MaxItemPerPage = Config.BBS_COMMISSION_COUNT_TO_PAGE;

		if(items > MaxItemPerPage)
		{
			double MaxPages = Math.ceil(items / MaxItemPerPage);

			pg.append("<center><table width=25 border=0><tr>");
			int ButtonInLine = 1;

			for(int current = 1; current <= MaxPages; current++)
			{
				if(page == current)
				{
					pg.append("<td width=25 align=center><button value=\"[").append(current).append("]\" width=28 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					if(search.isEmpty() || search.length() < 2)
					{
						pg.append("<td width=25 align=center><button value=\"").append(current).append("\" action=\"bypass _bbscommission:list-<?category?>-").append(current).append("\" width=28 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
					}
					else
					{
						pg.append("<td width=25 align=center><button value=\"").append(current).append("\" action=\"bypass _bbscommission:search-").append(search).append("-").append(current).append("\" width=28 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
					}
				}
				if(ButtonInLine == 18)
				{
					pg.append("</tr><tr>");
					ButtonInLine = 0;
				}
				ButtonInLine++;
			}
			pg.append("</tr></table></center>");
		}

		html = html.replace("<?content?>", content);
		html = html.replace("<?pages?>", pg.toString());
		html = html.replace("<?category?>", category);
		html = html.replace("<?category_name?>", getCategoryName(player, category) + (search.length() >= 2 ? " `" + search + "`" : ""));
		ShowBoard.separateAndSend(html, player);
	}

	private int getItemsCount(String category, String search, int owner_id)
	{
		int rowCount = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		long EndTime = 0;
		if(Config.BBS_COMMISSION_HIDE_OLD_ITEMS)
		{
			EndTime = System.currentTimeMillis();
		}

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(category.equalsIgnoreCase("all"))
				statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `endtime` > " + EndTime + "");
			else if(category.equalsIgnoreCase("myitems"))
			{
				statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `owner_id` = ?");
				statement.setInt(1, owner_id);
			}
			else if(search.length() >= 2)
				statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `endtime` > " + EndTime + " AND `item_name` LIKE '%" + search.toLowerCase() + "%'");
			else
			{
				statement = con.prepareStatement("SELECT COUNT(`id`) FROM `bbs_commission` WHERE `endtime` > " + EndTime + " AND `category` = ?");
				statement.setString(1, category);
			}
			rs = statement.executeQuery();
			if(rs.next())
				rowCount = rs.getInt(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}

		if(rowCount == 0)
			return 0;

		return rowCount;
	}

	private String getContent(Player player, int page, String category, String search, int owner_id)
	{
		StringBuilder sb = new StringBuilder();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		long EndTime = 0;
		if(Config.BBS_COMMISSION_HIDE_OLD_ITEMS)
		{
			EndTime = System.currentTimeMillis();
		}

		int limit1 = (page - 1) * Config.BBS_COMMISSION_COUNT_TO_PAGE;
		int limit2 = Config.BBS_COMMISSION_COUNT_TO_PAGE;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(category.equalsIgnoreCase("all"))
			{
				statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `endtime` > " + EndTime + " ORDER BY `date` DESC LIMIT ?, ?");
				statement.setInt(1, limit1);
				statement.setInt(2, limit2);
			}
			else if(category.equalsIgnoreCase("myitems"))
			{
				statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `owner_id` = ? ORDER BY `date` DESC LIMIT ?, ?");
				statement.setInt(1, owner_id);
				statement.setInt(2, limit1);
				statement.setInt(3, limit2);
			}
			else if(search.length() >= 2)
			{
				statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `endtime` > " + EndTime + " AND `item_name` LIKE '%" + search.toLowerCase() + "%' ORDER BY `date` DESC LIMIT ?, ?");
				statement.setInt(1, limit1);
				statement.setInt(2, limit2);
			}
			else
			{
				statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `endtime` > " + EndTime + " AND `category` = ? ORDER BY `date` DESC LIMIT ?, ?");
				statement.setString(1, category);
				statement.setInt(2, limit1);
				statement.setInt(3, limit2);
			}
			rset = statement.executeQuery();
			int i = 2;
			while(rset.next())
			{
				if(i == 1)
					i = 2;
				else
					i = 1;
				int id = rset.getInt("id");
				int item_id = rset.getInt("item_id");
				int price_id = rset.getInt("price_id");
				long price_count = rset.getLong("price_count");
				if(price_id == 0 || item_id == 0)
					continue;
				ItemTemplate item = ItemHolder.getInstance().getTemplate(item_id);
				if(item == null)
					continue;

				String icon = "l2ui_ch3.petitem_click";
				if(item.isPvP())
					icon = "icon.pvp_tab";
				else if(item.getItemId() == 9912)
					icon = "icon.fort_tab";

				if(item.isWeapon())
				{
					int enchant_level = rset.getInt("enchant_level");
					int augment_id = rset.getInt("augment_id");
					int attribute_fire = rset.getInt("attribute_fire");
					int attribute_water = rset.getInt("attribute_water");
					int attribute_wind = rset.getInt("attribute_wind");
					int attribute_earth = rset.getInt("attribute_earth");
					int attribute_holy = rset.getInt("attribute_holy");
					int attribute_unholy = rset.getInt("attribute_unholy");
					int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getAdditionalName().length() > 0 ? "-&nbsp;" + item.getAdditionalName() : "").append("</font> ").append(item.getCrystalType() != Grade.NONE ? "(" + item.getCrystalType().toString() + ")" : "").append("<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.price", player).addString(Util.formatAdena(price_count)).addString(" " + DifferentMethods.getItemName(price_id)).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(att_count > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(augment_id > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:show-").append(id).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else if(item.isArmor())
				{
					int enchant_level = rset.getInt("enchant_level");
					int attribute_fire = rset.getInt("attribute_fire");
					int attribute_water = rset.getInt("attribute_water");
					int attribute_wind = rset.getInt("attribute_wind");
					int attribute_earth = rset.getInt("attribute_earth");
					int attribute_holy = rset.getInt("attribute_holy");
					int attribute_unholy = rset.getInt("attribute_unholy");
					int att_count = attribute_fire + attribute_water + attribute_wind + attribute_earth + attribute_holy + attribute_unholy;
					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getAdditionalName().length() > 0 ? "-&nbsp;" + item.getAdditionalName() : "").append("</font> ").append(item.getCrystalType() != Grade.NONE ? "(" + item.getCrystalType().toString() + ")" : "").append("<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.price", player).addString(Util.formatAdena(price_count)).addString(" " + DifferentMethods.getItemName(price_id)).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(att_count > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.skill1352\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:show-").append(id).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else if(item.isAccessory() && (category.equalsIgnoreCase("all") || category.equalsIgnoreCase("jewelry") || category.equalsIgnoreCase("search")))
				{
					int enchant_level = rset.getInt("enchant_level");
					int augment_id = rset.getInt("augment_id");

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");
					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;<font color=ff8000>").append(item.getAdditionalName().length() > 0 ? "-&nbsp;" + item.getAdditionalName() : "").append("</font> ").append(item.getCrystalType() != Grade.NONE ? "(" + item.getCrystalType().toString() + ")" : "").append("<font color=00cccc>").append(enchant_level > 0 ? " +" + enchant_level : "").append("</font>");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.price", player).addString(Util.formatAdena(price_count)).addString(" " + DifferentMethods.getItemName(price_id)).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					if(item.isPvP())
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.pvp_point_i00\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"icon.pvp_tab\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(enchant_level > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_blessed_scrl_of_ench_wp_s_i05\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.multisell_plusicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					if(augment_id > 0)
					{
						sb.append("<td width=36 height=32 align=right>");
						sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"icon.etc_mineral_unique_i03\">");
						sb.append("<tr>");
						sb.append("<td valign=top align=center>");
						sb.append("<img src=\"l2ui_ch3.shortcut_recipeicon\" width=32 height=32>");
						sb.append("</td>");
						sb.append("</tr>");
						sb.append("</table>");
						sb.append("</td>");
						sb.append("<td width=6 height=32 align=right>");
						sb.append("</td>");
					}
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:show-").append(id).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
				else
				{
					int item_count = rset.getInt("item_count");

					sb.append("<table width=594 height=40 border=0>");
					sb.append("<tr>");
					sb.append("<td width=30 height=32 align=center>");
					sb.append("<table width=30 height=32 cellspacing=0 cellpadding=0 background=\"").append(item.getIcon()).append("\">");
					sb.append("<tr>");
					sb.append("<td valign=top align=center>");
					sb.append("<img src=\"").append(icon).append("\" width=32 height=32>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("<td width=400 align=left>");

					sb.append("<font color=LEVEL>").append(item.getName()).append("</font>&nbsp;(").append(new CustomMessage("communityboard.commission.shop.sell.count", player).addString(Util.formatAdena(item_count)).addString(DifferentMethods.declension(player, (int) item_count, "Piece")).toString()).append(")");
					sb.append("<br1>");
					sb.append(new CustomMessage("communityboard.commission.shop.price", player).addString(Util.formatAdena(price_count)).addString(" " + DifferentMethods.getItemName(price_id)).toString());
					sb.append("</td>");
					sb.append("<td width=160 align=right>");
					sb.append("<table width=150 height=32 cellspacing=0 cellpadding=0>");
					sb.append("<tr>");
					sb.append("<td width=36 height=32 align=right>");
					sb.append("<button action=\"bypass _bbscommission:show-").append(id).append("\" width=32 height=32 back=\"l2ui_ch3.QuestBtn_Down\" fore=\"l2ui_ch3.QuestBtn\">");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
					sb.append("<table width=594 border=0>");
					sb.append("<tr>");
					sb.append("<td width=594 height=10>");
					sb.append("<img src=\"l2ui.squaregray\" width=594 height=1>");
					sb.append("</td>");
					sb.append("</tr>");
					sb.append("</table>");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "<table width=594 border=0 bgcolor=433d32><tr><td width=594 height=38 align=center><br><font color=FF0000>Список товаров пуст</font></td></tr></table><br>";
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return sb.toString();
	}

	private void getItem(Player player, int id)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `bbs_commission` WHERE `id` = ? LIMIT 0, 1");
			statement.setInt(1, id);
			rset = statement.executeQuery();
			if(rset.next())
			{
				int item_id = rset.getInt("item_id");
				long item_count = rset.getLong("item_count");
				int owner_id = rset.getInt("owner_id");
				int price_id = rset.getInt("price_id");
				long price_count = rset.getLong("price_count");
				int enchant_level = rset.getInt("enchant_level");
				int augment_id = rset.getInt("augment_id");
				int attribute_fire = rset.getInt("attribute_fire");
				int attribute_water = rset.getInt("attribute_water");
				int attribute_wind = rset.getInt("attribute_wind");
				int attribute_earth = rset.getInt("attribute_earth");
				int attribute_holy = rset.getInt("attribute_holy");
				int attribute_unholy = rset.getInt("attribute_unholy");

				PcInventory inventory = player.getInventory();

				// Не забираем оплату, если забирает хозяин
				if(owner_id != player.getObjectId())
				{
					if(!DifferentMethods.getPay(player, price_id, price_count, true))
						return;
					else
						Functions.removeItem(player, price_id, price_count);

					// Отсылаем письмо с уведомлением.
					Mail mail = new Mail();
					mail.setSenderId(1);
					mail.setSenderName(StringHolder.getInstance().getNotNull(player, "communityboard.commission.npc"));
					mail.setReceiverId(owner_id);
					mail.setReceiverName(getCharName(owner_id));
					mail.setTopic(StringHolder.getInstance().getNotNull(player, "communityboard.commission.title"));
					mail.setBody(StringHolder.getInstance().getNotNull(player, "communityboard.commission.text"));
					mail.setPrice(0);
					mail.setUnread(true);

					ItemInstance item = ItemFunctions.createItem(price_id);
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(price_count);
					item.save();

					// Прикрепляем оплату.
					mail.addAttachment(item);
					mail.setUnread(true);
					mail.setType(Mail.SenderType.NEWS_INFORMER);

					// Утсонавливаем срок письма.
					mail.setExpireTime(Config.BBS_COMMISSION_MAIL_TIME * 720 * 120 + (int) (System.currentTimeMillis() / 1000L));
					mail.save();

					Player target = World.getPlayer(owner_id);
					if(target != null)
					{
						target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						target.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
					}
				}
				ItemInstance item = ItemFunctions.createItem(item_id);

				// Если стопковый предмет, ставим количество
				if(item.isStackable())
					item.setCount(item_count);

				// Добавляем заточку, если есть
				if(enchant_level > 0)
					item.setEnchantLevel(enchant_level);

				// Добавляем аугментацию, если есть
				if(augment_id > 0)
					item.setAugmentationId(augment_id);

				// Добавляем аттрибуты, если есть
				if(attribute_fire > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.WATER) : Element.FIRE, attribute_fire);
				if(attribute_water > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.FIRE) : Element.WATER, attribute_water);
				if(attribute_wind > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.EARTH) : Element.WIND, attribute_wind);
				if(attribute_earth > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.WIND) : Element.EARTH, attribute_earth);
				if(attribute_holy > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.UNHOLY) : Element.HOLY, attribute_holy);
				if(attribute_unholy > 0)
					item.setAttributeElement(item.isArmor() ? Element.getReverseElement(Element.HOLY) : Element.UNHOLY, attribute_unholy);

				player.sendPacket(SystemMessage2.obtainItems(item));
				inventory.addItem(item);
				deleteItem(con, id);
				if(owner_id != player.getObjectId())
					Log.add("bought " + item.getName() + " for a " + price_count + " " + DifferentMethods.getItemName(price_id), "CommunityBoardCommission: ", player);
				else
					Log.add("withdrawn " + item.getName(), "CommunityBoardCommission: ", player);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void deleteItem(Connection con, int id)
	{
		PreparedStatement statement = null;
		try
		{
			// Удаляем предмет с аукциона
			statement = con.prepareStatement("DELETE FROM `bbs_commission` WHERE `id` = ?");
			statement.setInt(1, id);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private String strip(String text)
	{
		text = text.replaceAll("<a>", "");
		text = text.replaceAll("</a>", "");
		text = text.replaceAll("<font>", "");
		text = text.replaceAll("</font>", "");
		text = text.replaceAll("<table>", "");
		text = text.replaceAll("<tr>", "");
		text = text.replaceAll("<td>", "");
		text = text.replaceAll("</table>", "");
		text = text.replaceAll("</tr>", "");
		text = text.replaceAll("</td>", "");
		text = text.replaceAll("<br>", "");
		text = text.replaceAll("<br1>", "");
		text = text.replaceAll("<button", "");
		return text;
	}

	private String getCategoryName(Player player, String category)
	{
		if(category.equalsIgnoreCase("all"))
			return new CustomMessage("communityboard.commission.category.all", player).toString();
		else if(category.equalsIgnoreCase("matherials"))
			return new CustomMessage("communityboard.commission.category.matherials", player).toString();
		else if(category.equalsIgnoreCase("pieces"))
			return new CustomMessage("communityboard.commission.category.pieces", player).toString();
		else if(category.equalsIgnoreCase("recipe"))
			return new CustomMessage("communityboard.commission.category.recipe", player).toString();
		else if(category.equalsIgnoreCase("weapon"))
			return new CustomMessage("communityboard.commission.category.weapon", player).toString();
		else if(category.equalsIgnoreCase("armor"))
			return new CustomMessage("communityboard.commission.category.armor", player).toString();
		else if(category.equalsIgnoreCase("jewelry"))
			return new CustomMessage("communityboard.commission.category.jewelry", player).toString();
		else if(category.equalsIgnoreCase("other"))
			return new CustomMessage("communityboard.commission.category.other", player).toString();
		else if(category.equalsIgnoreCase("search"))
			return new CustomMessage("communityboard.commission.category.search", player).toString();
		else if(category.equalsIgnoreCase("myitems"))
			return new CustomMessage("communityboard.commission.category.myitems", player).toString();
		else if(category.equalsIgnoreCase("add"))
			return new CustomMessage("communityboard.commission.category.add", player).toString();
		else if(category.equalsIgnoreCase("select"))
			return new CustomMessage("communityboard.commission.category.select", player).toString();

		return new CustomMessage("communityboard.commission.category.unknown", player).toString();
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}

	private boolean checkItem(ItemInstance item)
	{
		if(ArrayUtils.contains(Config.BBS_COMMISSION_ALLOW_ITEMS, item.getItemId()))
			return true;

		if(item.getTemplate().isPvP() && !Config.BBS_COMMISSION_ALLOW_PVP)
			return false;

		if(item.getEnchantLevel() > Config.BBS_COMMISSION_MAX_ENCHANT)
			return false;

		if(!item.getTemplate().isTradeable() && !item.getTemplate().isPvP())
			return false;

		if(ArrayUtils.contains(Config.BBS_COMMISSION_NOT_ALLOW_ITEMS, item.getItemId()))
			return false;

		if(item.isShadowItem())
			return false;

		if(item.getTemplate().isQuest())
			return false;

		if(item.getTemplate().isHerb())
			return false;

		if(item.getTemplate().isTerritoryFlag())
			return false;

		if(item.isCommonItem())
			return false;

		if(item.isTemporalItem())
			return false;

		if(item.isHeroWeapon())
			return false;

		if(item.isEquipped() && !Config.BBS_COMMISSION_ALLOW_EQUIPPED)
			return false;

		if(item.getTemplate().isUnderwear() && !Config.BBS_COMMISSION_ALLOW_UNDERWEAR)
			return false;

		if(item.getTemplate().isCloak() && !Config.BBS_COMMISSION_ALLOW_CLOAK)
			return false;

		if(item.getTemplate().isBracelet() && !Config.BBS_COMMISSION_ALLOW_BRACELET)
			return false;

		if(item.isAugmented() && !Config.BBS_COMMISSION_ALLOW_AUGMENTED)
			return false;

		return true;
	}
}