package actions;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.RaidBossInstance;
import jts.gameserver.model.reward.RewardData;
import jts.gameserver.model.reward.RewardGroup;
import jts.gameserver.model.reward.RewardList;
import jts.gameserver.model.reward.RewardType;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.stats.Stats;
import jts.gameserver.utils.HtmlUtils;

import org.apache.commons.lang3.StringUtils;

public abstract class RewardListInfo
{
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	public static int SHOW_ITEM_ON_PAGE = 30; // Количество итемов на страницу

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}
	public static void showMainInfo(Player player, NpcInstance npc, int type, int show_page)
	{
		NpcHtmlMessage htmlMessage = new NpcHtmlMessage(5);
		StringBuilder builder = new StringBuilder(1000);

		htmlMessage.setFile("actions/rewardlist_info_empty.htm");

		if(type == 0)
		{
			builder.append("<html noscrollbar><title>").append(npc.getNpcId()).append("</title><body>");
			builder.append("<table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
			builder.append("<tr>");
			builder.append("<td valign=\"top\" align=\"center\"><br>");
			builder.append("<table width=235>");
			builder.append("<tr>");
			builder.append("<td width=235><center><font color=\"FFFF00\" name=\"hs12\">").append(HtmlUtils.htmlNpcName(npc.getNpcId())).append(" ").append("</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1></center></td>");
			builder.append("</tr>");
			builder.append("</table>");
			builder.append("</td>");
			builder.append("</tr>");
			builder.append("<tr><td align=center>");
			builder.append("<table width=\"250\">");
			builder.append("<tr>");
			builder.append("<td align=center><button value=\"").append(player.isLangRus() ? "Рейтируемый дроплист" : "Rated droplist").append("\" action=\"bypass -h scripts_actions.OnActionShift:droplist 1\" width=190 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			builder.append("</tr>");
			builder.append("<tr>");
			builder.append("<td align=center><button value=\"").append(player.isLangRus() ? "Не рейтируемый дроплист" : "Not rated droplist").append("\" action=\"bypass -h scripts_actions.OnActionShift:droplist 2\" width=190 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			builder.append("</tr>");
			builder.append("<tr>");
			builder.append("<td align=center><button value=\"").append(player.isLangRus() ? "Не рейтируемая группа" : "Not rated group").append("\" action=\"bypass -h scripts_actions.OnActionShift:droplist 3\" width=190 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			builder.append("</tr>");
			builder.append("<tr>");
			builder.append("<td align=center><button value=\"").append(player.isLangRus() ? "Спойл" : "Sweep").append("\" action=\"bypass -h scripts_actions.OnActionShift:droplist 4\" width=190 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
			builder.append("</tr>");
			builder.append("</table>");
			builder.append("</td></tr>");
			builder.append("</table>");
			builder.append("</body></html>");

			htmlMessage.replace("%info%", builder.toString());
			player.sendPacket(htmlMessage);
		}
		else
			showInfo(player, npc, type, show_page);
	}
	public static void showInfo(Player player, NpcInstance npc, int type, int show_page)
	{
		final int diff = npc.calculateLevelDiffForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());
		double mod = npc.calcStat(Stats.REWARD_MULTIPLIER, 1., player, null);
		mod *= Experience.penaltyModifier(diff, 9);

		NpcHtmlMessage htmlMessage = new NpcHtmlMessage(5);
		htmlMessage.replace("%npc_name%", HtmlUtils.htmlNpcName(npc.getNpcId()));

		if(mod <= 0)
		{
			htmlMessage.setFile("actions/rewardlist_to_weak.htm");
			player.sendPacket(htmlMessage);
			return;
		}

		if(npc.getTemplate().getRewards().isEmpty())
		{
			htmlMessage.setFile("actions/rewardlist_empty.htm");
			player.sendPacket(htmlMessage);
			return;
		}

		htmlMessage.setFile("actions/rewardlist_info.htm");

		StringBuilder builder = new StringBuilder(1000);
		for(Map.Entry<RewardType, RewardList> entry : npc.getTemplate().getRewards().entrySet())
		{
			RewardList rewardList = entry.getValue();

			switch(entry.getKey())
			{
				case RATED_GROUPED:
					if(type == 1)
						ratedGroupedRewardList(builder, npc, rewardList, player, mod, show_page);
					break;
				case NOT_RATED_GROUPED:
					if(type == 2)
						notRatedGroupedRewardList(builder, rewardList, mod);
					break;
				case NOT_RATED_NOT_GROUPED:
					if(type == 3)
						notGroupedRewardList(builder, rewardList, 1.0, mod);
					break;
				case SWEEP:
					if(type == 4)
						notGroupedRewardList(builder, rewardList, Config.RATE_DROP_SPOIL * player.getRateSpoil(), mod);
					break;
			}
		}
		htmlMessage.replace("%info%", builder.toString());
		player.sendPacket(htmlMessage);
	}


	public static void ratedGroupedRewardList(StringBuilder tmp, NpcInstance npc, RewardList list, Player player, double mod, int show_page)
	{
		tmp.append("<table width=100%>");
		tmp.append("<tr><td><table width=270 border=0><tr><td><font color=\"FFFAFA\"><center>").append("").append("</center></font></td></tr></table></td></tr>");

		tmp.append("</table>");

		int n_item = 0;
		int remove_top_item = (show_page * SHOW_ITEM_ON_PAGE) - SHOW_ITEM_ON_PAGE;
		int all_item = 0;

		for(RewardGroup g : list)
		{
			List<RewardData> items = g.getItems();

			all_item += items.size();

			if(remove_top_item - items.size() > 0 || n_item == SHOW_ITEM_ON_PAGE)
			{
				remove_top_item = remove_top_item - items.size();
				continue;
			}

			double gchance = g.getChance();
			double gmod = mod;
			double grate;
			double gmult;

			double SiegeGuard = 1.0;

			if(player.getNetConnection().getBonus() > 1)
				SiegeGuard = Config.RATE_DROP_SIEGE_GUARD_FOR_PREMIUM;

			double rateDrop = npc instanceof RaidBossInstance ? Config.RATE_DROP_RAIDBOSS : npc.isSiegeGuard() ? Config.RATE_DROP_SIEGE_GUARD * SiegeGuard : Config.RATE_DROP_ITEMS * player.getRateItems();
			double rateAdena = Config.RATE_DROP_ADENA * player.getRateAdena();

			if(g.isAdena())
			{
				if(rateAdena == 0)
					continue;

				grate = rateAdena;

				if(gmod > 10)
				{
					gmod *= g.getChance() / RewardList.MAX_CHANCE;
					gchance = RewardList.MAX_CHANCE;
				}

				grate *= gmod;
			}
			else
			{
				if(rateDrop == 0)
					continue;

				grate = rateDrop;

				if(g.notRate())
					grate = Math.min(gmod, 1.0);
				else
					grate *= gmod;
			}

			gmult = Math.ceil(grate);

			tmp.append("<br><center><img src=\"L2UI.SquareWhite\" width=270 height=1></center>");
			tmp.append("<table width=100%>");
			tmp.append("<tr><td>");
			tmp.append("<table width=270 bgcolor=333333>");
			tmp.append("<tr><td width=270><center><font color=\"FFFF00\">Шанс Группы: ").append(pf.format(gchance / RewardList.MAX_CHANCE)).append("</font></center></td>");
			tmp.append("</tr>");
			tmp.append("</table>").append("</td></tr>");
			tmp.append("</table>");
			tmp.append("<center><img src=\"L2UI.SquareWhite\" width=270 height=1></center><br>");
			tmp.append("<table width=100% border=0>");

			for(RewardData d : items)
			{
				if(remove_top_item != 0)
				{
					remove_top_item--;
					continue;
				}
				if(n_item == SHOW_ITEM_ON_PAGE)
				{
					break;
				}
				else
				{
					n_item++;
				}

				double imult = d.notRate() ? 1.0 : gmult;
				String icon = d.getItem().getIcon();
				if(icon == null || icon.equals(StringUtils.EMPTY))
					icon = "icon.etc_question_mark_i00";
				tmp.append("<tr><td><table>");
				tmp.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238><font color=\"FFFAFA\">").append(HtmlUtils.htmlItemName(d.getItemId())).append("</font><br1>");
				tmp.append("[<font color=\"b09979\">Min: </font>").append(Math.round(d.getMinDrop() * (g.isAdena() ? gmult : 1.0))).append("<font color=\"b09979\"> Max: </font>").append(Math.round(d.getMaxDrop() * imult)).append("]&nbsp;");
				tmp.append("[<font color=\"b09979\">Шанс: </font>").append(pf.format(d.getChance() / RewardList.MAX_CHANCE)).append("]</td></tr>");
				tmp.append("</table></td></tr>");
			}
			tmp.append("</table>");
		}

		// Навигация
		double old_page = Math.ceil((double) all_item / (double) SHOW_ITEM_ON_PAGE);
		if(old_page > 1)
		{
			tmp.append("<br><center><img src=\"L2UI.SquareWhite\" width=270 height=1></center><br><br>");
			tmp.append("<center><table width=100%><tr>");
			for(int i = 1; i <= (int) old_page; i++)
			{
				if(i == show_page)
				{
					tmp.append("<td width=10><font color=\"b09979\">[" + i + "]</font></td> ");
				}
				else
				{
					tmp.append("<td width=10><a action=\"bypass -h scripts_actions.OnActionShift:droplist 1 " + i + "\">[" + i + "]</a></td> ");
				}
			}
			tmp.append("</tr></table></center>");
		}
	}
	public static void notRatedGroupedRewardList(StringBuilder tmp, RewardList list, double mod)
	{
		tmp.append("<table width=270>");
		tmp.append("<tr><td><table width=270 border=0><tr><td><center><font color=\"aaccff\">").append("").append("</font></center></td></tr></table></td></tr>");
		tmp.append("</table>");

		for(RewardGroup g : list)
		{
			List<RewardData> items = g.getItems();
			double gchance = g.getChance();

			tmp.append("<br><center><img src=\"L2UI.SquareWhite\" width=270 height=1></center>");
			tmp.append("<table width=100%>");
			tmp.append("<tr><td>");
			tmp.append("<table width=270 bgcolor=333333>");
			tmp.append("<tr><td width=270><center><font color=\"FFFF00\">Шанс Группы: ").append(pf.format(gchance / RewardList.MAX_CHANCE)).append("</font></center></td>");
			tmp.append("</tr>");
			tmp.append("</table>").append("</td></tr>");
			tmp.append("</table>");
			tmp.append("<center><img src=\"L2UI.SquareWhite\" width=270 height=1></center><br>");
			tmp.append("<table width=100%>");
			for(RewardData d : items)
			{
				String icon = d.getItem().getIcon();
				if(icon == null || icon.equals(StringUtils.EMPTY))
					icon = "icon.etc_question_mark_i00";
				tmp.append("<tr><td><table>");
				tmp.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238><font color=\"LEVEL\">").append(HtmlUtils.htmlItemName(d.getItemId())).append("</font><br1>");
				tmp.append("[<font color=\"b09979\">Min: </font>").append(Math.round(d.getMinDrop())).append("<font color=\"b09979\"> Max: </font>").append(Math.round(d.getMaxDrop())).append("]&nbsp;");
				tmp.append("[<font color=\"b09979\">Шанс: </font>").append(pf.format(d.getChance() / RewardList.MAX_CHANCE)).append("]</td></tr>");
				tmp.append("</table></td></tr>");
			}
			tmp.append("</table>");
		}
	}
	public static void notGroupedRewardList(StringBuilder tmp, RewardList list, double rate, double mod)
	{
		tmp.append("<tr><td><table width=270 border=0><tr><td><font color=\"FFFAFA\"><center>").append("").append("</center></font></td></tr></table></td></tr>");
		tmp.append("<table width=100%>");

		tmp.append("<tr><td><table>");
		for(RewardGroup g : list)
		{
			List<RewardData> items = g.getItems();
			double gmod = mod;
			double grate;
			double gmult;

			if(rate == 0)
				continue;

			grate = rate;

			if(g.notRate())
				grate = Math.min(gmod, 1.0);
			else
				grate *= gmod;

			gmult = Math.ceil(grate);

			for(RewardData d : items)
			{
				double imult = d.notRate() ? 1.0 : gmult;
				String icon = d.getItem().getIcon();
				if(icon == null || icon.equals(StringUtils.EMPTY))
					icon = "icon.etc_question_mark_i00";
				tmp.append("<tr><td width=32><img src=").append(icon).append(" width=32 height=32></td><td width=238><font color=\"LEVEL\">").append(HtmlUtils.htmlItemName(d.getItemId())).append("</font><br1>");
				tmp.append("[<font color=\"b09979\">Min: </font>").append(d.getMinDrop()).append("<font color=\"b09979\"> Max: </font>").append(Math.round(d.getMaxDrop() * imult)).append("]&nbsp;");
				tmp.append("[<font color=\"b09979\">Шанс: </font>").append(pf.format(d.getChance() / RewardList.MAX_CHANCE)).append("]</td></tr>");
			}
		}

		tmp.append("</table></td></tr>");
		tmp.append("</table>");
	}
}