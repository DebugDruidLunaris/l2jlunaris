package services;

import java.util.Date;

import jts.gameserver.Config;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.dao.AccountBonusDAO;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.BonusRequest;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.Bonus;
import jts.gameserver.network.serverpackets.ExBR_PremiumState;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Util;

public class RateBonus extends Functions
{
	public void list()
	{
		Player player = getSelf();
		if(Config.SERVICES_RATE_TYPE == Bonus.NO_BONUS)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		String html = null;
		if(player.getNetConnection().getBonus() <= 1)
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonus.htm", player);
			String add = "";
			for(int i = 0; i < Config.SERVICES_RATE_BONUS_DAYS.length; i++)
			{
				add += "<button value=\"" + new CustomMessage("communityboard.cabinet.premium.button", player).addString(String.valueOf(Config.SERVICES_RATE_BONUS_VALUE[i])).addString(Config.SERVICES_RATE_BONUS_DAYS[i] + " " + DifferentMethods.declension(player, Config.SERVICES_RATE_BONUS_DAYS[i], "Days")) + "\" action=\"bypass -h scripts_services.RateBonus:get " + i + "\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\"><br1>" + new CustomMessage("scripts.services.cost", player).addNumber(Config.SERVICES_RATE_BONUS_PRICE[i]).addString(DifferentMethods.getItemName(Config.SERVICES_RATE_BONUS_ITEM[i])) + "";
			}
			html = html.replaceFirst("%toreplace%", add);
		}
		else
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player);
			int endtime = player.getNetConnection().getBonusExpire();
			if(endtime >= System.currentTimeMillis() / 1000L)
				html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player).replaceFirst("endtime", new Date(endtime * 1000L).toString());
		}
		show(html, player);
	}

	public void list2()
	{
		Player player = getSelf();
		if(Config.SERVICES_RATE_TYPE == Bonus.NO_BONUS)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		String html = null;
		String sItemName = null;
		if(player.getNetConnection().getBonus() <= 1)
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusDaysForConfig.htm", player);

			for(int i = 0; i < Config.SERVICES_RATE_BONUS_DAYS.length; i++)
			{
				if(Config.SERVICES_RATE_BONUS_PERDAY_ITEM == -100)
					sItemName = "PC Cafe Points";
				else
					sItemName = ItemHolder.getInstance().getTemplate(Config.SERVICES_RATE_BONUS_PERDAY_ITEM).getName();
			}
			html = html.replaceFirst("%rate%", Double.toString(Config.SERVICES_RATE_BONUS_PERDAY_VALUE));
			html = html.replaceFirst("%configdays%", Integer.toString(Config.SERVICES_RATE_BONUS_PERDAY_PRICE));
			html = html.replaceFirst("%itemname%", sItemName);
		}
		else
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player);
			int endtime = player.getNetConnection().getBonusExpire();
			if(endtime >= System.currentTimeMillis() / 1000L)
				html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player).replaceFirst("endtime", new Date(endtime * 1000L).toString());
		}
		show(html, player);
	}

	public void get(String[] param)
	{
		Player player = getSelf();
		if(Config.SERVICES_RATE_TYPE == Bonus.NO_BONUS)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}
		int i = Integer.parseInt(param[0]);

		switch(Config.SERVICES_RATE_BONUS_ITEM[i])
		{
			case -100:
				if(player.getPcBangPoints() < Config.SERVICES_RATE_BONUS_PRICE[i])
				{
					player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
					return;
				}
				else
					player.reducePcBangPoints(Config.SERVICES_RATE_BONUS_PRICE[i]);
				break;
			case 0:
				if(player.getPremiumPoints() < Config.SERVICES_RATE_BONUS_PRICE[i])
				{
					//TODO правильное сообщение
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
					return;
				}
				else
					player.reducePremiumPoints(Config.SERVICES_RATE_BONUS_PRICE[i]);
				break;
			default:
				if(!player.getInventory().destroyItemByItemId(Config.SERVICES_RATE_BONUS_ITEM[i], Config.SERVICES_RATE_BONUS_PRICE[i]))
				{
					if(Config.SERVICES_RATE_BONUS_ITEM[i] == 57)
						player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					else
						player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				break;
		}

		if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_LOGINSERVER && LoginServerCommunication.getInstance().isShutdown())
		{
			list();
			return;
		}
		Log.add(player.getName() + "|" + player.getObjectId() + "|rate bonus|" + Config.SERVICES_RATE_BONUS_VALUE[i] + "|" + Config.SERVICES_RATE_BONUS_DAYS[i] + "|", "services");
		double bonus = Config.SERVICES_RATE_BONUS_VALUE[i];
		int bonusExpire = (int) (System.currentTimeMillis() / 1000L) + Config.SERVICES_RATE_BONUS_DAYS[i] * 24 * 60 * 60;
		switch(Config.SERVICES_RATE_TYPE)
		{
			case Bonus.BONUS_GLOBAL_ON_LOGINSERVER:
				LoginServerCommunication.getInstance().sendPacket(new BonusRequest(player.getAccountName(), bonus, bonusExpire));
				break;
			case Bonus.BONUS_GLOBAL_ON_GAMESERVER:
				AccountBonusDAO.getInstance().insert(player.getAccountName(), bonus, bonusExpire);
				break;
		}
		player.getNetConnection().setBonus(bonus);
		player.getNetConnection().setBonusExpire(bonusExpire);
		player.stopBonusTask();
		player.startBonusTask();
		if(player.getParty() != null)
			player.getParty().recalculatePartyData();
		player.sendPacket(new ExBR_PremiumState(player, true));
		show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", player), player);
		player.broadcastPacket(new MagicSkillUse(player, player, 6176, 1, 1000, 0));
	}

	public void get2(String[] param)
	{
		int days = 0;
		Player player = getSelf();
		if(Config.SERVICES_RATE_TYPE == Bonus.NO_BONUS)
		{
			show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
			return;
		}

		if(!Util.isMatchingRegexp(param[0], "[0-9]{1,3}"))
		{
			show(HtmCache.getInstance().getNotNull("scripts/services/incorrect_string.htm", player), player);
			return;
		}
		else
		{
			if(Integer.parseInt(param[0]) < 1 || Integer.parseInt(param[0]) > 999)
			{
				show(HtmCache.getInstance().getNotNull("scripts/services/incorrect_string.htm", player), player);
				return;
			}
			else
				days = Integer.parseInt(param[0]);
		}

		switch(Config.SERVICES_RATE_BONUS_PERDAY_ITEM)
		{
			case -100:
				if(player.getPcBangPoints() < Config.SERVICES_RATE_BONUS_PERDAY_PRICE * days)
				{
					player.sendPacket(SystemMsg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
					return;
				}
				else
					player.reducePcBangPoints(Config.SERVICES_RATE_BONUS_PERDAY_PRICE * days);
				break;
			case 0:
				if(player.getPremiumPoints() < Config.SERVICES_RATE_BONUS_PERDAY_PRICE * days)
				{
					//TODO правильное сообщение
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
					return;
				}
				else
					player.reducePremiumPoints(Config.SERVICES_RATE_BONUS_PERDAY_PRICE * days);
				break;
			default:
				if(!player.getInventory().destroyItemByItemId(Config.SERVICES_RATE_BONUS_PERDAY_ITEM, Config.SERVICES_RATE_BONUS_PERDAY_PRICE * days))
				{
					if(Config.SERVICES_RATE_BONUS_PERDAY_ITEM == 57)
						player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					else
						player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				break;
		}

		if(Config.SERVICES_RATE_TYPE == Bonus.BONUS_GLOBAL_ON_LOGINSERVER && LoginServerCommunication.getInstance().isShutdown())
		{
			list2();
			return;
		}
		Log.add(player.getName() + "|" + player.getObjectId() + "|rate bonus|" + Config.SERVICES_RATE_BONUS_PERDAY_VALUE + "|" + days + "|", "services");
		double bonus = Config.SERVICES_RATE_BONUS_PERDAY_VALUE;
		int bonusExpire = (int) (System.currentTimeMillis() / 1000L) + days * 24 * 60 * 60;
		switch(Config.SERVICES_RATE_TYPE)
		{
			case Bonus.BONUS_GLOBAL_ON_LOGINSERVER:
				LoginServerCommunication.getInstance().sendPacket(new BonusRequest(player.getAccountName(), bonus, bonusExpire));
				break;
			case Bonus.BONUS_GLOBAL_ON_GAMESERVER:
				AccountBonusDAO.getInstance().insert(player.getAccountName(), bonus, bonusExpire);
				break;
		}
		player.getNetConnection().setBonus(bonus);
		player.getNetConnection().setBonusExpire(bonusExpire);
		player.stopBonusTask();
		player.startBonusTask();
		if(player.getParty() != null)
			player.getParty().recalculatePartyData();
		player.sendPacket(new ExBR_PremiumState(player, true));
		show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", player), player);
		player.broadcastPacket(new MagicSkillUse(player, player, 6176, 1, 1000, 0));
	}
}