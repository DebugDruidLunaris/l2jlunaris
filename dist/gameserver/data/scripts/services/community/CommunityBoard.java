package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.Shutdown;
import jts.gameserver.cache.Msg;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.BuyListHolder;
import jts.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExBuySellList;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import jts.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import jts.gameserver.network.serverpackets.ShopPreviewList;
import jts.gameserver.network.serverpackets.ShowCalc;
import jts.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.scripts.Scripts;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.Util;

import org.apache.commons.lang3.math.NumberUtils;

public class CommunityBoard implements ScriptFile, ICommunityBoardHandler
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
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] 
		{
			"_bbshome",
			"_bbswear",
			"_bbsaugment",
			"_bbsdeaugment",
			"_bbssell",
			"_bbscabinet:clan",
			"_bbscabinet:premium",
			"_bbscabinet:configuration",
			"_bbscabinet:show",
			"_bbscabinet:security",
			"_bbscabinet:password",
			"_bbscabinet:change:password",
			"_bbscabinet:games",
			"_bbscabinet:cfg",
			"_bbslink",
			"_bbsmultisell",
			"_bbspage",
			"_bbsscripts",
			"_bbswashsins",
			"_bbsclearpk",
			"_bbswiki",
			"_bbsmammon",
			"_bbsservices",
			"_bbssupportmagic",
			"_bbsinfo",
			"_bbsevent",
			"_bbsdonate",
			"_bbsvip",
			"_bbsshop",
			"_bbsshow",
			"_bbshairstyle:index",
			"_bbssecurity:lockip",
			"_bbssecurity:unlockip",
			"_bbssecurity:lockhwid",
			"_bbssecurity:unlockhwid",
			"_bbshairstyle:page",
			"_bbshairstyle:change" 
		};
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		// CustomMessage - Mensagens internacionais.
		CustomMessage No = new CustomMessage("common.result.no", player);
		CustomMessage Yes = new CustomMessage("common.result.yes", player);
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		if("bbshome".equals(cmd))
		{
			StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT, "_");
			String dafault = p.nextToken();
			if(dafault.equals(cmd))
			{
				html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/bbs_top.htm", player);

				int favCount = 0;
				Connection con = null;
				PreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
					statement.setInt(1, player.getObjectId());
					rset = statement.executeQuery();
					if(rset.next())
					{
						favCount = rset.getInt("cnt");
					}
				}
				catch(Exception e) {}
				finally
				{
					DbUtils.closeQuietly(con, statement, rset);
				}

				html = html.replace("<?fav_count?>", String.valueOf(favCount));
				html = html.replace("<?clan_count?>", String.valueOf(ClanTable.getInstance().getClans().length));
				html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
			}
			else
			{
				onBypassCommand(player, Config.BBS_DEFAULT);
				return;
			}
		}
		else if("bbslink".equals(cmd))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/bbs_homepage.htm", player);
		}
		else if(bypass.startsWith("_bbsshop"))
		{
			// Example: "bypass _bbsshop:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/shop/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsdonate"))
		{
			// Example: "bypass _bbsdonate:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/donate/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsevent"))
		{
			// Example: "bypass _bbsevent:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/event/" + page + ".htm", player);
		}
		
		else if(bypass.startsWith("_bbsinfo"))
		{
			// Example: "bypass _bbsinfo:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/info/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsservices"))
		{
			// Example: "bypass _bbsservices:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/services/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbswiki"))
		{
			// Example: "bypass _bbswiki:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsmammon"))
		{
	        if(!Config.ALLOW_BBS_MAMMON)
			{
				player.sendMessage(player.isLangRus() ? "Сервис Маммонов Отключен" : "Warehouse offline");
				String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/error.htm", player);
				ShowBoard.separateAndSend(content, player);
				return;
			}
	        if (player == null) return;
			if(!Config.BBS_MAMMON_ALLOW_PEACE_ZONE)
			if (!player.isInPeaceZone())
			{
	            player.sendMessage(player.isLangRus() ? "Запрещено использовать Маммонов за пределами города" : "You may not use a mammon outside the city");
				return;
			}
			if(player.isInTrade())
			{
	            player.sendMessage(player.isLangRus() ? "Запрещено использовать Маммонов пока вы торгуете" : "You may not use mammon while you are trading");
				return;
			}

			if(player.isFishing())
			{
	            player.sendMessage(player.isLangRus() ? "Запрещено использовать Маммонов пока вы ловите рыбу" : "You may not use mammon while you are fishing");
				return;
			}
			if(player.getEnchantScroll() != null)
			{
	            player.sendMessage(player.isLangRus() ? "Запрещено использовать Маммонов пока вы затачиваете предмет" : "You may not use mammon until you hone the subject");
				return;
			}
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/mammon/mammon.htm", player);
		}
		else if(bypass.startsWith("_bbswear"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			int shopId = Integer.parseInt(mBypass[1]);
			CBshowWearWindow(player, shopId);
			return;
		}
		else if(bypass.startsWith("_bbsmultisell"))
		{
			// Example: "_bbsmultisell:10000;_bbspage:index" or
			// "_bbsmultisell:10000;_bbshome" or "_bbsmultisell:10000"...
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
				{
					handler.onBypassCommand(player, pBypass);
				}
			}

			int listId = Integer.parseInt(mBypass[1]);
			MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
			return;
		}
		else if(bypass.startsWith("_bbssell"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			@SuppressWarnings("unused")
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}
			player.setIsBBSUse(true);
			NpcTradeList list = BuyListHolder.getInstance().getBuyList(-1);
			player.sendPacket(new ExBuySellList.BuyList(list, player, 0.), new ExBuySellList.SellRefundList(player, false));
			return;
		}
	      else if (bypass.startsWith("_bbsaugment"))
	      {
	          String pBypass[] = bypass.split(";");
	          if (pBypass.length > 1)
	          {
	              ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass[1]);
	              if (handler != null)
	                  handler.onBypassCommand(player, pBypass[1]);
	          }
	          player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
	          return;
	      }
	      else if (bypass.startsWith("_bbsdeaugment"))
	      {
	          String pBypass[] = bypass.split(";");
	          if (pBypass.length > 1)
	          {
	              ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass[1]);
	              if (handler != null)
	                  handler.onBypassCommand(player, pBypass[1]);
	          }
	          player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
	          return;
	      }
		else if(bypass.startsWith("_bbsvip"))
		{
			// Example: "bypass _bbsvip:index".
			String[] b = bypass.split(":");
			String page = b[1];
			int price = Config.BBS_VIP_SECTION_PRICE;
			ItemTemplate itemName = ItemHolder.getInstance().getTemplate(Config.BBS_VIP_SECTION_ITEM_ID);

			int enoughItemCount = (int) (Config.BBS_VIP_SECTION_PRICE - player.getInventory().getCountOf(Config.BBS_VIP_SECTION_ITEM_ID));
			if(player.getInventory().getCountOf(Config.BBS_VIP_SECTION_ITEM_ID) < price)
			{
				player.sendMessage(player.isLangRus() ? "Quantidade insuficiente para acessar a sessão VIP " + enoughItemCount + " " + itemName.getName() + "." : "Quantidade insuficiente para acessar a sessão VIP " + enoughItemCount + " " + itemName.getName() + ".");
				player.sendPacket(new ExShowScreenMessage(player.isLangRus() ? "Quantidade insuficiente para acessar a sessão VIP " + enoughItemCount + " " + itemName.getName() + "." : "Quantidade insuficiente para acessar a sessão VIP " + enoughItemCount + " " + itemName.getName() + ".", 3000, ScreenMessageAlign.TOP_CENTER, true));
				DifferentMethods.communityNextPage(player, "_bbshome");
				return;
			}
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/vip/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbspage"))
		{
			// Example: "bypass _bbspage:index".
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/pages/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbsmultisell"))
		{
			// Example: "_bbsmultisell:10000;_bbspage:index" or
			// "_bbsmultisell:10000;_bbshome" or "_bbsmultisell:10000"...
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
				{
					handler.onBypassCommand(player, pBypass);
				}
			}

			int listId = Integer.parseInt(mBypass[1]);
			MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
			return;
		}
		else if(bypass.startsWith("_bbsscripts"))
		{
			// Example: "_bbsscripts:events.GvG.GvG:addGroup;_bbspage:index" or
			// "_bbsscripts:events.GvG.GvG:addGroup;_bbshome" or
			// "_bbsscripts:events.GvG.GvG:addGroup"...
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String sBypass = st2.nextToken().substring(12);
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
				{
					handler.onBypassCommand(player, pBypass);
				}
			}

			String[] word = sBypass.split("\\s+");
			String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
				return;

			Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args });
			return;
		}
		else if(bypass.startsWith("_bbswashsins"))
		{
			CommunityBoardWashSins.wash(player);
			return;
		}
		else if(bypass.startsWith("_bbsclearpk"))
		{
			CommunityBoardClearPK.clear(player);
			return;
		}
		else if(bypass.startsWith("_bbssupportmagic"))
		{
			CommunityBoardSupportMagic.doSupportMagic(player, true, true);
			return;
		}
		else if(bypass.startsWith("_bbssecurity:lockip"))
		{
			CommunityBoardSecurity.lock(player, true, false);
			return;
		}
		else if(bypass.startsWith("_bbssecurity:unlockip"))
		{
			CommunityBoardSecurity.unlock(player, true, false);
			return;
		}
		else if(bypass.startsWith("_bbssecurity:lockhwid"))
		{
			CommunityBoardSecurity.lock(player, false, true);
			return;
		}
		else if(bypass.startsWith("_bbssecurity:unlockhwid"))
		{
			CommunityBoardSecurity.unlock(player, false, true);
			return;
		}
		else if(bypass.startsWith("_bbscabinet:show"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/index.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:premium"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/premium.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:games"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/games/index.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:configuration"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/configuration.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:cfg"))
		{
			String[] function = bypass.split(":");
			if(function[2].equals("lang"))
			{
				String[] lang = bypass.split(" ");
				if(lang[1].equals("en"))
					player.setVar("lang@", "en", -1);
				else if(lang[1].equals("ru"))
					player.setVar("lang@", "ru", -1);
			}
			else if(Config.SERVICES_ENABLE_NO_CARRIER && function[2].equals("nocarrier"))
			{
				String[] second = bypass.split(" ");
				int time = NumberUtils.toInt(second[1], Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
				if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
					time = Config.SERVICES_NO_CARRIER_MAX_TIME;
				else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
					time = Config.SERVICES_NO_CARRIER_MIN_TIME;
				player.setVar("noCarrier", String.valueOf(time), -1);
			}
			else if(function[2].equals("droplisticons"))
			{
				if(function[3].equals("on"))
					player.setVar("DroplistIcons", "1", -1);
				else if(function[3].equals("off"))
					player.unsetVar("DroplistIcons");
			}
			else if(function[2].equals("exp"))
			{
				if(function[3].equals("on"))
					player.setVar("NoExp", "1", -1);
				else if(function[3].equals("off"))
					player.unsetVar("NoExp");
			}
			else if(function[2].equals("notraders"))
			{
				if(function[3].equals("on"))
				{
					player.setNotShowTraders(true);
					player.setVar("notraders", "1", -1);
				}
				else if(function[3].equals("off"))
				{
					player.setNotShowTraders(false);
					player.unsetVar("notraders");
				}
			}
			else if(function[2].equals("showbuffanim"))
			{
				if(function[3].equals("on"))
				{
					player.setNotShowBuffAnim(true);
					player.setVar("notShowBuffAnim", "1", -1);
				}
				else if(function[3].equals("off"))
				{
					player.setNotShowBuffAnim(false);
					player.unsetVar("notShowBuffAnim");
				}
			}
			else if(function[2].equals("skillchance"))
			{
				if(function[3].equals("on"))
					player.setVar("SkillsHideChance", "1", -1L);
				else if(function[3].equals("off"))
					player.unsetVar("SkillsHideChance");
			}
			else if(function[2].equals("autoloot"))
			{
				if(function[3].equals("on"))
					player.setAutoLoot(true);
				else if(function[3].equals("off"))
					player.setAutoLoot(false);
			}
			else if(function[2].equals("autolootherbs"))
			{
				if(function[3].equals("on"))
					player.setAutoLootHerbs(true);
				else if(function[3].equals("off"))
					player.setAutoLootHerbs(false);
			}

			DifferentMethods.communityNextPage(player, "_bbscabinet:configuration");
			return;
		}
		else if(bypass.startsWith("_bbscabinet:clan"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/clan.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:password"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/cabinet/password.htm", player);
		}
		else if(bypass.startsWith("_bbscabinet:change:password"))
		{
			String[] s = bypass.split(" ");
			String old;
			String newPass1;
			String newPass2;
			String n1;
			String n2;
			String captcha;
			try
			{
				old = s[1];
				newPass1 = s[2];
				newPass2 = s[3];
				n1 = s[4];
				n2 = s[5];
				captcha = s[6];

				CommunityBoardCabinet.changePassword(player, old, newPass1, newPass2, n1, n2, captcha);
			}
			catch(Exception e)
			{
				player.setPasswordResult(new CustomMessage("communityboard.cabinet.password.incorrect.input", player).toString());
			}
			DifferentMethods.communityNextPage(player, "_bbscabinet:password");
			return;
		}
		else if(bypass.startsWith("_bbsshow:calculator"))
		{
			String[] s = bypass.split(";");
			String link = s[1];
			DifferentMethods.communityNextPage(player, link);
			player.sendPacket(new ShowCalc(4393));
			return;
		}
		else if(bypass.startsWith("_bbshairstyle:index"))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/hair/index.htm", player);
		}
		else if(bypass.startsWith("_bbshairstyle:page"))
		{
			String[] b = bypass.split("-");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/hair/" + page + ".htm", player);
		}
		else if(bypass.startsWith("_bbshairstyle:change"))
		{
			String[] b = bypass.split(" ");
			String race = b[1];
			String id = b[2];
			CommunityBoardCabinet.changeHairStyle(player, race, id);
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/hair/" + race + ".htm", player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(bypass) + "</center><br><br></body></html>", player);
		}

		html = html.replace("<?cb_time?>", String.valueOf(DifferentMethods.time()));
		html = html.replace("<?password_change_result?>", String.valueOf(player.isLangRus() ? "<font color=\"33CC33\">Заполните все необходимые поля.</font>" : "<font color=\"33CC33\">Fill all necessary fields.</font>"));
		html = html.replace("<?n1?>", String.valueOf(CommunityBoardCabinet.doCaptcha(true, false)));
		html = html.replace("<?n2?>", String.valueOf(CommunityBoardCabinet.doCaptcha(false, true)));
		html = html.replace("<?cb_online_players?>", String.valueOf(GameObjectsStorage.getAllPlayersCount() - GameObjectsStorage.getAllOfflineCount() + Config.CBB_ONLINE_CHEAT_COUNT));
		html = html.replace("<?cb_offtrade_players?>", String.valueOf(GameObjectsStorage.getAllOfflineCount() + Config.CBB_OFFTRADE_CHEAT_COUNT));
		html = html.replace("<?premium_buttons?>", String.valueOf(CommunityBoardPremiumAccount.button(player)));
		html = html.replace("<?premium_buttons_cabinet?>", String.valueOf(CommunityBoardPremiumAccount.buttonCab(player)));
		html = html.replace("<?premium_img?>", String.valueOf(CommunityBoardPremiumAccount.images(player)));
		html = html.replace("<?player_premium?>", String.valueOf(CommunityBoardPremiumAccount.consider(player)));
		html = html.replace("<?player_noobless?>", String.valueOf(player.isNoble() ? "<font color=\"18FF00\">" + Yes + "</font>" : player.getSubLevel() > 75 ? "<font color=\"FF0000\">" + No + "</font>" : new CustomMessage("communityboard.noble.info", player)));
		html = html.replace("<?player_name?>", String.valueOf(player.getName()));
		html = html.replace("<?player_level?>", String.valueOf(player.getLevel()));
		html = html.replace("<?player_pvp?>", String.valueOf(player.getPvpKills()));
		html = html.replace("<?player_pk?>", String.valueOf(player.getPkKills()));
		html = html.replace("<?online_time?>", String.valueOf(player.getOnlineTime(player)));
		html = html.replace("<?player_ip?>", String.valueOf(player.getIP()));
		html = html.replace("<?player_class?>", String.valueOf(HtmlUtils.htmlClassNameNonClient(player, player.getClassId().getId())));
		html = html.replace("<?player_clan?>", String.valueOf(player.getClan() != null ? new CustomMessage("communityboard.clan.info", player).addString(player.getClan().getName()).addNumber(player.getClan().getLevel()) : "<font color=\"FF0000\">" + No + "</font>"));
		html = html.replace("<?player_clan1?>", String.valueOf(player.getClan() != null ? player.getClan().getName() : "<font color=\"FF0000\">" + No + "</font>"));
		html = html.replace("<?player_ally?>", String.valueOf(player.getClan() != null && player.getClan().getAlliance() != null ? player.getClan().getAlliance().getAllyName() : "<font color=\"FF0000\">" + No + "</font>"));
		html = html.replace("<?bind_ip?>", String.valueOf(CommunityBoardSecurity.check(player, true, false, false, false)));
		html = html.replace("<?bind_hwid?>", String.valueOf(CommunityBoardSecurity.check(player, false, true, false, false)));
		html = html.replace("<?allow_ip?>", String.valueOf(CommunityBoardSecurity.check(player, false, false, true, false)));
		html = html.replace("<?allow_hwid?>", String.valueOf(CommunityBoardSecurity.check(player, false, false, false, true)));
    	html = html.replace("<?restart?>", Util.formatTime2(Shutdown.getInstance().getSeconds()));

		html = html.replace("<?player_lang?>", CommunityBoardCabinet.lang(player).toUpperCase());
		html = html.replace("<?player_dli?>", CommunityBoardCabinet.DroplistIcons(player, false));
		html = html.replace("<?button_dli?>", CommunityBoardCabinet.DroplistIcons(player, true));

		html = html.replace("<?player_noe?>", CommunityBoardCabinet.NoExp(player, false));
		html = html.replace("<?button_noe?>", CommunityBoardCabinet.NoExp(player, true));

		html = html.replace("<?player_notraders?>", CommunityBoardCabinet.NotShowTraders(player, false));
		html = html.replace("<?button_notraders?>", CommunityBoardCabinet.NotShowTraders(player, true));

		html = html.replace("<?player_notShowBuffAnim?>", CommunityBoardCabinet.notShowBuffAnim(player, false));
		html = html.replace("<?button_notShowBuffAnim?>", CommunityBoardCabinet.notShowBuffAnim(player, true));

		html = html.replace("<?player_skill_chance?>", CommunityBoardCabinet.SkillsHideChance(player, false));
		html = html.replace("<?button_skill_chance?>", CommunityBoardCabinet.SkillsHideChance(player, true));

		html = html.replace("<?player_autolooth?>", CommunityBoardCabinet.AutoLoot(player, false));
		html = html.replace("<?button_autolooth?>", CommunityBoardCabinet.AutoLoot(player, true));

		html = html.replace("<?player_autolooth_herbs?>", CommunityBoardCabinet.AutoLootHerbs(player, false));
		html = html.replace("<?button_autolooth_herbs?>", CommunityBoardCabinet.AutoLootHerbs(player, true));

		html = html.replace("<?player_noCarrier?>", Config.SERVICES_ENABLE_NO_CARRIER ? player.getVarB("noCarrier") ? "<font color=\"LEVEL\">" + player.getVar("noCarrier") + "</font>" : "<font color=\"LEVEL\">0</font>" : "<font color=\"FF0000\">N/A</font>");

		String[] adminNames, supportNames, gmNames, forumNames;

		adminNames = Config.COMMUNITYBOARD_SERVER_ADMIN_NAME.split(";");
		supportNames = Config.COMMUNITYBOARD_SERVER_SUPPORT_NAME.split(";");
		gmNames = Config.COMMUNITYBOARD_SERVER_GM_NAME.split(";");
		forumNames = Config.COMMUNITYBOARD_FORUM_ADMIN_NAME.split(";");

		for(int i = 0; i < adminNames.length; i++)
		{
			html = html.replace("<?server_admin_name_" + i + "?>", adminNames[i]);
			if(GameObjectsStorage.getPlayer(adminNames[i]) != null && GameObjectsStorage.getPlayer(adminNames[i]).isOnline())
			{
				html = html.replace("<?server_admin_" + i + "_status?>", player.isLangRus() ? "<font color=\"18FF00\">Online</font>" : "<font color=\"18FF00\">Online</font>");
			}
			else
			{
				html = html.replace("<?server_admin_" + i + "_status?>", player.isLangRus() ? "<font color=\"FF0000\">offline</font>" : "<font color=\"FF0000\">Offline</font>");
			}
		}

		for(int i = 0; i < supportNames.length; i++)
		{
			html = html.replace("<?server_support_name_" + i + "?>", supportNames[i]);
			if(GameObjectsStorage.getPlayer(supportNames[i]) != null && GameObjectsStorage.getPlayer(supportNames[i]).isOnline())
			{
				html = html.replace("<?server_support_" + i + "_status?>", player.isLangRus() ? "<font color=\"18FF00\">Online</font>" : "<font color=\"18FF00\">Online</font>");
			}
			else
			{
				html = html.replace("<?server_support_" + i + "_status?>", player.isLangRus() ? "<font color=\"FF0000\">offline</font>" : "<font color=\"FF0000\">Offline</font>");
			}
		}

		for(int i = 0; i < gmNames.length; i++)
		{
			html = html.replace("<?server_gm_name_" + i + "?>", gmNames[i]);
			if(GameObjectsStorage.getPlayer(gmNames[i]) != null && GameObjectsStorage.getPlayer(gmNames[i]).isOnline())
			{
				html = html.replace("<?server_gm_" + i + "_status?>", player.isLangRus() ? "<font color=\"18FF00\">Online</font>" : "<font color=\"18FF00\">Online</font>");
			}
			else
			{
				html = html.replace("<?server_gm_" + i + "_status?>", player.isLangRus() ? "<font color=\"FF0000\">offline</font>" : "<font color=\"FF0000\">Offline</font>");
			}
		}

		for(int i = 0; i < forumNames.length; i++)
		{
			html = html.replace("<?server_forum_name_" + i + "?>", forumNames[i]);
		}

		ShowBoard.separateAndSend(html, player);
	}
	
	private void CBshowWearWindow(Player player, int shopId)
	{
		NpcTradeList list = BuyListHolder.getInstance().getBuyList(shopId);
		ShopPreviewList bl = new ShopPreviewList(list, player);
		player.sendPacket(bl);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}