package services.community;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.base.Element;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.Func;
import jts.gameserver.stats.funcs.FuncAdd;
import jts.gameserver.stats.funcs.FuncDiv;
import jts.gameserver.stats.funcs.FuncEnchant;
import jts.gameserver.stats.funcs.FuncMul;
import jts.gameserver.stats.funcs.FuncSet;
import jts.gameserver.stats.funcs.FuncSub;
import jts.gameserver.stats.funcs.FuncTemplate;
import jts.gameserver.templates.item.ArmorTemplate;
import jts.gameserver.templates.item.EtcItemTemplate;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.WeaponTemplate;
import jts.gameserver.utils.HtmlUtils;
import jts.gameserver.utils.Language;
import jts.gameserver.utils.Util;

import org.apache.commons.lang3.StringUtils;

public class CommunityBoardItemInfo implements ScriptFile, ICommunityBoardHandler
{
	private static CommunityBoardItemInfo _Instance = null;
	private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
	private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
	private String val1 = "";
	private String val2 = "";
	private String val3 = "";
	private String val4 = "";

	static
	{
		pf.setMaximumFractionDigits(4);
		df.setMinimumFractionDigits(2);
	}

	public static CommunityBoardItemInfo getInstance()
	{
		if(_Instance == null)
			_Instance = new CommunityBoardItemInfo();
		return _Instance;
	}

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().registerHandler(this);
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
		return new String[] 
		{
			"_bbsitemlist",
			"_bbsitematributes",
			"_bbsitemstats",
			"_bbsitemskills",
			"_bbsarmorinfoid",
			"_bbsarmorinfoname",
			"_bbsweaponinfoid",
			"_bbsweaponinfoname",
			"_bbsiteminfoid",
			"_bbsiteminfoname" 
		};
	}

	@Override
	public void onBypassCommand(Player activeChar, String command)
	{
        if (activeChar== null) return;
        if(!Config.ALLOW_BBS_WIKI)
		{
        	activeChar.sendMessage(activeChar.isLangRus() ? "База знаний временно недоступна" : "Knowledge Base is temporarily unavailable");
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/error.htm", activeChar);
			ShowBoard.separateAndSend(content, activeChar);
			return;
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();

		val1 = "";
		val2 = "";
		val3 = "";
		val4 = "";

		if(st.countTokens() == 1)
			val1 = st.nextToken();
		else if(st.countTokens() == 2)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
		}
		else if(st.countTokens() == 3)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
			val3 = st.nextToken();
		}
		else if(st.countTokens() == 4)
		{
			val1 = st.nextToken();
			val2 = st.nextToken();
			val3 = st.nextToken();
			val4 = st.nextToken();
		}

		if(cmd.equalsIgnoreCase("_bbsitemlist"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/list.htm", activeChar);
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsarmorinfoid"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateArmorInfo(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);

		}
		else if(cmd.equalsIgnoreCase("_bbsarmorinfoname"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			String str = null;

			if(!val1.equals(""))
				str = val1;

			if(!val2.equals(""))
				str = val1 + " " + val2;

			if(!val3.equals(""))
				str = val1 + " " + val2 + " " + val3;

			if(!val4.equals(""))
				str = val1 + " " + val2 + " " + val3 + " " + val4;

			content = content.replace("%iteminfo%", generateArmorInfo(activeChar, str));
			ShowBoard.separateAndSend(content, activeChar);

		}
		else if(cmd.equalsIgnoreCase("_bbsweaponinfoid"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateWeaponInfo(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsweaponinfoname"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			String str = null;

			if(!val1.equals(""))
				str = val1;

			if(!val2.equals(""))
				str = val1 + " " + val2;

			if(!val3.equals(""))
				str = val1 + " " + val2 + " " + val3;

			if(!val4.equals(""))
				str = val1 + " " + val2 + " " + val3 + " " + val4;

			content = content.replace("%iteminfo%", generateWeaponInfo(activeChar, str));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsiteminfoid"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateItemInfo(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsiteminfoname"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			String str = null;

			if(!val1.equals(""))
				str = val1;

			if(!val2.equals(""))
				str = val1 + " " + val2;

			if(!val3.equals(""))
				str = val1 + " " + val2 + " " + val3;

			if(!val4.equals(""))
				str = val1 + " " + val2 + " " + val3 + " " + val4;

			content = content.replace("%iteminfo%", generateItemInfo(activeChar, str));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsitemskills"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateItemSkills(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsitemstats"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateItemStats(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
		else if(cmd.equalsIgnoreCase("_bbsitematributes"))
		{
			String content = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/wiki/iteminfo/iteminfo.htm", activeChar);
			content = content.replace("%iteminfo%", generateItemAttribute(activeChar, Integer.parseInt(val1)));
			ShowBoard.separateAndSend(content, activeChar);
		}
	}

	private String generateItemSkills(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		result.append("<table width=690 border=0>");

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);

		String str;
		if(temp.isWeapon())
			str = "_bbsweaponinfoid";
		else if(temp.isArmor() || temp.isAccessory())
			str = "_bbsarmorinfoid";
		else
			str = "_bbsiteminfoid";

		for(Skill skill : temp.getAttachedSkills())
		{
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(skill.getIcon()).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append(new CustomMessage("communityboard.wiki.items.skill.name", player).addSkillName(skill)).append("<br1>").append(new CustomMessage("communityboard.wiki.items.skill.id", player).addNumber(skill.getId())).append(new CustomMessage("communityboard.wiki.items.skill.level", player).addNumber(skill.getLevel()));
			result.append("</td>");
			result.append("</tr>");
		}

		result.append("</table>");

		result.append(InfoButton(player, str, temp.getItemId()));
		return result.toString();
	}

	private String generateItemStats(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		result.append("<table width=690 border=0>");

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);

		String str;
		if(temp.isWeapon())
			str = "_bbsweaponinfoid";
		else if(temp.isArmor() || temp.isAccessory())
			str = "_bbsarmorinfoid";
		else
			str = "_bbsiteminfoid";

		for(FuncTemplate func : temp.getAttachedFuncs())
			if(getFunc(player, func) != null)
				result.append("<tr><td>› <font color=\"b09979\">").append(getFunc(player, func)).append("</font></td></tr><br>");

		result.append("</table>");

		result.append(InfoButton(player, str, temp.getItemId()));
		return result.toString();
	}

	private String generateItemAttribute(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);

		String str;
		if(temp.isWeapon())
			str = "_bbsweaponinfoid";
		else if(temp.isArmor() || temp.isAccessory())
			str = "_bbsarmorinfoid";
		else
			str = "_bbsiteminfoid";

		if(temp.getBaseAttributeValue(Element.FIRE) > 0)
		{
			result.append(AttributeHtml(player, "etc_fire_stone_i00", new CustomMessage("common.element.0", player).toString(), temp.getBaseAttributeValue(Element.FIRE)));
		}

		if(temp.getBaseAttributeValue(Element.WATER) > 0)
		{
			result.append(AttributeHtml(player, "etc_water_stone_i00", new CustomMessage("common.element.1", player).toString(), temp.getBaseAttributeValue(Element.WATER)));
		}

		if(temp.getBaseAttributeValue(Element.WIND) > 0)
		{
			result.append(AttributeHtml(player, "etc_wind_stone_i00", new CustomMessage("common.element.2", player).toString(), temp.getBaseAttributeValue(Element.WIND)));
		}

		if(temp.getBaseAttributeValue(Element.EARTH) > 0)
		{
			result.append(AttributeHtml(player, "etc_earth_stone_i00", new CustomMessage("common.element.3", player).toString(), temp.getBaseAttributeValue(Element.EARTH)));
		}

		if(temp.getBaseAttributeValue(Element.HOLY) > 0)
		{
			result.append(AttributeHtml(player, "etc_holy_stone_i00", new CustomMessage("common.element.4", player).toString(), temp.getBaseAttributeValue(Element.HOLY)));
		}

		if(temp.getBaseAttributeValue(Element.UNHOLY) > 0)
		{
			result.append(AttributeHtml(player, "etc_unholy_stone_i00", new CustomMessage("common.element.5", player).toString(), temp.getBaseAttributeValue(Element.UNHOLY)));
		}

		result.append(InfoButton(player, str, temp.getItemId()));
		return result.toString();
	}

	private String generateItemInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();

		for(ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
			if(temp != null && !temp.isArmor() && !temp.isWeapon() && !temp.isAccessory() && (temp.getName() == name || val2.equals("") ? temp.getName().startsWith(name) : temp.getName().contains(name) || temp.getName().equals(name) || temp.getName().equalsIgnoreCase(name)))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsiteminfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}

		return result.toString();
	}

	private String generateItemInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if(temp != null && !temp.isArmor() && !temp.isWeapon() && !temp.isAccessory())
		{
			EtcItemTemplate etcitem = (EtcItemTemplate) temp;
			String icon = etcitem.getIcon();
			if(icon == null || icon.equals(StringUtils.EMPTY))
				icon = "icon.etc_question_mark_i00";

			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Nome do artigo:</font> " : "Item name:</font> ").append(HtmlUtils.htmlItemName(etcitem.getItemId())).append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "Item ID:</font> ").append(etcitem.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Item Type: " : "Item Type: ").append("</font>").append(etcitem.getItemType().toString()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Weight: " : "Weight: ").append("</font>").append(etcitem.getWeight()).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Sale Price: " : "Sale Price: ").append("</font>").append(Util.formatAdena(etcitem.getReferencePrice() / 2)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "It will be Docked: " : "It will be docked: ").append("</font>").append(etcitem.isStackable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "A temporary items: " : "A temporary items: ").append("</font>").append(etcitem.getDurability() > 0 ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "You can throw: " : "You can throw: ").append("</font>").append(etcitem.isDropable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Can be Sold: " : "Can be Sold: ").append("</font>").append(etcitem.isSellable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Can be Exchanged: " : "Can be Exchanged: ").append("</font>").append(etcitem.isStoreable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");

			if(etcitem.getAttachedSkills().length > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.skill", player).toString(), "_bbsitemskills", etcitem.getItemId()));
			}
		}
		else
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=755><tr><td width=755><center><font name=\"hs12\" color=\"FF0000\">Item não encontrado</font></center></td></tr></table><br>" : "<table width=755><tr><td width=755><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");

		return result.toString();
	}

	private String generateWeaponInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();

		for(ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
			if(temp != null && temp.isWeapon() && (temp.getName() == name || val2.equals("") ? temp.getName().startsWith(name) : temp.getName().contains(name) || temp.getName().equals(name) || temp.getName().equalsIgnoreCase(name)))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsweaponinfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}

		return result.toString();
	}

	private String generateWeaponInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if(temp != null && temp.isWeapon())
		{
			WeaponTemplate weapon = (WeaponTemplate) temp;
			String icon = weapon.getIcon();
			if(icon == null || icon.equals(StringUtils.EMPTY))
				icon = "icon.etc_question_mark_i00";

			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Item Name:</font> " : "Item name:</font> ").append(HtmlUtils.htmlItemName(weapon.getItemId())).append(" (<font color=\"b09979\">").append(weapon.getItemType().toString()).append("</font>)<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "Item ID:</font> ").append(weapon.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Weapon Grade: " : "Weapon grade: ").append("</font>").append(weapon.getCrystalType()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Slot: " : "Slot: ").append("</font>").append(getBodyPart(player, weapon)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Crystallized: " : "Divided into crystals: ").append("</font>").append(weapon.isCrystallizable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			if(weapon.isCrystallizable())
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Number of Crystals: " : "Number of crystals: ").append("</font>").append(weapon.getCrystalCount()).append("&nbsp;").append("<br>");
			}
			else
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов:</font> 0" : "Number of crystals:</font> 0").append("&nbsp;").append("<br>");
			}
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление спиритов: " : "Consume spiritshot: ").append("</font>").append(weapon.getSpiritShotCount()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Оружие камаелей: " : "Kamael weapons: ").append("</font>").append(weapon.getKamaelConvert() > 0 ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Вес: " : "Weight: ").append("</font>").append(weapon.getWeight()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Цена продажи: " : "Sale price: ").append("</font>").append(Util.formatAdena(weapon.getReferencePrice() / 2)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Будет стыковаться: " : "It will be docked: ").append("</font>").append(weapon.isStackable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Временный предмет: " : "A temporary item: ").append("</font>").append(weapon.getDurability() > 0 ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно выбросить: " : "You can throw: ").append("</font>").append(weapon.isDropable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Реюз Атаки: " : "Attack reuse: ").append("</font>").append(weapon.getAttackReuseDelay() / 1000).append(" сек.").append("&nbsp;").append("</font><br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно продать: " : "Can be sold: ").append("</font>").append(weapon.isSellable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить аугментацию: " : "You can insert the argument: ").append("</font>").append(weapon.isAugmentable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить атрибут: " : "You can insert an attribute: ").append("</font>").append(weapon.isAttributable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно обменять: " : "Can be exchanged: ").append("</font>").append(weapon.isStoreable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление сосок: " : "Consume soulshot: ").append("</font>").append(weapon.getSoulShotCount()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Потребление МП: " : "Consume Mp: ").append("</font>").append(weapon.getMpConsume()).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");

			if(weapon.getAttachedSkills().length > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.skill", player).toString(), "_bbsitemskills", weapon.getItemId()));
			}

			if(weapon.getAttachedFuncs().length > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.bonus", player).toString(), "_bbsitemstats", weapon.getItemId()));
			}

			if(weapon.getBaseAttributeValue(Element.FIRE) > 0 || weapon.getBaseAttributeValue(Element.WATER) > 0 || weapon.getBaseAttributeValue(Element.WIND) > 0 || weapon.getBaseAttributeValue(Element.EARTH) > 0 || weapon.getBaseAttributeValue(Element.HOLY) > 0 || weapon.getBaseAttributeValue(Element.UNHOLY) > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.att", player).toString(), "_bbsitematributes", weapon.getItemId()));
			}
		}
		else
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Предмет не найден</font></center></td></tr></table><br>" : "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");

		return result.toString();
	}

	private String generateArmorInfo(Player player, String name)
	{
		StringBuilder result = new StringBuilder();

		for(ItemTemplate temp : ItemHolder.getInstance().getAllTemplates())
			if(temp != null && (temp.isArmor() || temp.isAccessory()) && (temp.getName() == name || val2.equals("") ? temp.getName().startsWith(name) : temp.getName().contains(name) || temp.getName().startsWith(name) || temp.getName().equals(name) || temp.getName().equalsIgnoreCase(name)))
			{
				result.append("<center><table width=690>");
				result.append("<tr>");
				result.append("<td WIDTH=690 align=center valign=top>");
				result.append("<center><button value=\"");
				result.append(temp.getName());
				result.append("\" action=\"bypass _bbsarmorinfoid ").append(temp.getItemId()).append("\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></center>");
				result.append("</td>");
				result.append("</tr>");
				result.append("</table></center>");
			}

		return result.toString();
	}

	private String generateArmorInfo(Player player, int id)
	{
		StringBuilder result = new StringBuilder();

		ItemTemplate temp = ItemHolder.getInstance().getTemplate(id);
		if(temp != null && (temp.isArmor() || temp.isAccessory()))
		{
			ArmorTemplate armor = (ArmorTemplate) temp;
			String icon = armor.getIcon();
			if(icon == null || icon.equals(StringUtils.EMPTY))
				icon = "icon.etc_question_mark_i00";

			result.append("<center><table width=690>");
			result.append("<tr>");
			result.append("<td WIDTH=690 align=center valign=top>");
			result.append("<table border=0 cellspacing=4 cellpadding=3>");
			result.append("<tr>");
			result.append("<td FIXWIDTH=50 align=right valign=top>");
			result.append("<img src=\"").append(icon).append("\" width=32 height=32>");
			result.append("</td>");
			result.append("<td FIXWIDTH=671 align=left valign=top>");
			result.append("<font color=\"0099FF\">").append(player.getLanguage() == Language.RUSSIAN ? "Название предмета:</font> " : "Item name:</font> ").append(HtmlUtils.htmlItemName(armor.getItemId())).append("<br1><font color=\"LEVEL\">").append(player.getLanguage() == Language.RUSSIAN ? "ID предмета:</font> " : "Item ID:</font> ").append(armor.getItemId()).append("&nbsp;");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<table border=0 cellspacing=0 cellpadding=0>");
			result.append("<tr>");
			result.append("<td width=690>");
			result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
			result.append("<br><table width=690>");
			result.append("<tr>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Тип доспехов: " : "Armor type: ").append("</font>").append(armor.getItemType().toString()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Грейд доспехов: " : "Armor grade: ").append("</font>").append(armor.getCrystalType()).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Слот: " : "Slot: ").append("</font>").append(getBodyPart(player, armor)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Разбивается на кристаллы: " : "Divided into crystals: ").append("</font>").append(armor.isCrystallizable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			if(armor.isCrystallizable())
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов: " : "Number of crystals: ").append("</font>").append(armor.getCrystalCount()).append("&nbsp;").append("<br>");
			}
			else
			{
				result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Количество кристаллов:</font> 0" : "Number of crystals:</font> 0").append("&nbsp;").append("<br>");
			}
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Вес: " : "Weight: ").append("</font>").append(armor.getWeight()).append("&nbsp;").append("</font><br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Цена продажи: " : "Sale price: ").append("</font>").append(Util.formatAdena(armor.getReferencePrice() / 2)).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Будет стыковаться: " : "It will be docked: ").append("</font>").append(armor.isStackable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Временный предмет: " : "A temporary item: ").append("</font>").append(armor.getDurability() > 0 ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно выбросить: " : "You can throw: ").append("</font>").append(armor.isDropable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("<td>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно продать: " : "Can be sold: ").append("</font>").append(armor.isSellable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить аугментацию: " : "You can insert the argument: ").append("</font>").append(armor.isAugmentable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно вставить атрибут: " : "You can insert an attribute: ").append("</font>").append(armor.isAttributable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("› <font color=\"b09979\">").append(player.getLanguage() == Language.RUSSIAN ? "Можно обменять: " : "Can be exchanged: ").append("</font>").append(armor.isStoreable() ? (new CustomMessage("common.result.yes", player)) : (new CustomMessage("common.result.no", player))).append("&nbsp;").append("<br>");
			result.append("</td>");
			result.append("</tr>");
			result.append("</table></center>");

			if(armor.getAttachedSkills().length > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.skill", player).toString(), "_bbsitemskills", armor.getItemId()));
			}

			if(armor.getAttachedFuncs().length > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.bonus", player).toString(), "_bbsitemstats", armor.getItemId()));
			}

			if(armor.getBaseAttributeValue(Element.FIRE) > 0 || armor.getBaseAttributeValue(Element.WATER) > 0 || armor.getBaseAttributeValue(Element.WIND) > 0 || armor.getBaseAttributeValue(Element.EARTH) > 0 || armor.getBaseAttributeValue(Element.HOLY) > 0 || armor.getBaseAttributeValue(Element.UNHOLY) > 0)
			{
				result.append(Button(player, new CustomMessage("communityboard.wiki.items.list.att", player).toString(), "_bbsitematributes", armor.getItemId()));
			}
		}
		else
			result.append(player.getLanguage() == Language.RUSSIAN ? "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Предмет не найден</font></center></td></tr></table><br>" : "<table width=690><tr><td width=690><center><font name=\"hs12\" color=\"FF0000\">Item not found</font></center></td></tr></table><br>");

		return result.toString();
	}

	private String getFunc(Player player, FuncTemplate func)
	{
		if(func.getFunc(null) != null)
		{
			String str;
			Func f = func.getFunc(null);
			if(getStats(player, f) != null)
				if(f instanceof FuncAdd)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Increases ";
					return str + getStats(player, f) + " на " + f.value;
				}
				else if(f instanceof FuncSet)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Добавляет " : "Sets ";
					return str + getStats(player, f) + " в " + f.value;
				}
				else if(f instanceof FuncSub)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Уменьшает " : "Decreases ";
					return str + getStats(player, f) + " на " + f.value;
				}
				else if(f instanceof FuncMul)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Умножает " : "Multiplies ";
					return str + getStats(player, f) + " на " + f.value;
				}
				else if(f instanceof FuncDiv)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Делит " : "Divides ";
					return str + getStats(player, f) + " на " + f.value;
				}
				else if(f instanceof FuncEnchant)
				{
					str = player.getLanguage() == Language.RUSSIAN ? "Увеличивает " : "Increases in the sharpening ";
					return str + getStats(player, f) + " на " + f.value;
				}
		}
		return new CustomMessage("common.not.recognized", player).toString();
	}

	private String getStats(Player player, Func f)
	{
		String str;
		if(f.stat == Stats.MAX_HP)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "максимальное ХП" : "max HP";
			return str;
		}
		else if(f.stat == Stats.MAX_MP)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "максимальное МП" : "max MP";
			return str;
		}
		else if(f.stat == Stats.MAX_CP)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "максимальное СП" : " max CP";
			return str;
		}
		else if(f.stat == Stats.REGENERATE_HP_RATE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "регенерация ХП" : "regeneration HP";
			return str;
		}
		else if(f.stat == Stats.REGENERATE_CP_RATE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "регенерация СП" : "regeneration CP";
			return str;
		}
		else if(f.stat == Stats.REGENERATE_MP_RATE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "регенерация МП" : "regeneration MP";
			return str;
		}
		else if(f.stat == Stats.RUN_SPEED)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "скорость" : "speed";
			return str;
		}
		else if(f.stat == Stats.POWER_DEFENCE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "физическую защиту" : "physical defence";
			return str;
		}
		else if(f.stat == Stats.MAGIC_DEFENCE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "магическую защиту" : "magical defence";
			return str;
		}
		else if(f.stat == Stats.POWER_ATTACK)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "физическую атаку" : "physical attack";
			return str;
		}
		else if(f.stat == Stats.MAGIC_ATTACK)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "магическую атаку" : "magical attack";
			return str;
		}
		else if(f.stat == Stats.ATK_REUSE || f.stat == Stats.ATK_BASE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "реюз атаку" : "reuse attack";
			return str;
		}
		else if(f.stat == Stats.EVASION_RATE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "точность" : "avoid";
			return str;
		}
		else if(f.stat == Stats.ACCURACY_COMBAT)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "уклонение" : "evasion";
			return str;
		}
		else if(f.stat == Stats.CRITICAL_BASE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "шанс критического удара" : "crit";
			return str;
		}
		else if(f.stat == Stats.SHIELD_DEFENCE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "защиту щитом" : "defense shield";
			return str;
		}
		else if(f.stat == Stats.SHIELD_RATE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "шанс уклониться щитом" : "chance to avoid a shield";
			return str;
		}
		else if(f.stat == Stats.POWER_ATTACK_RANGE)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "радиус физической атаки" : "reuse physical attack";
			return str;
		}
		else if(f.stat == Stats.STAT_STR)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "СИЛ" : "STR";
			return str;
		}
		else if(f.stat == Stats.STAT_CON)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "ВЫН" : "CON";
			return str;
		}
		else if(f.stat == Stats.STAT_DEX)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "ЛВК" : "DEX";
			return str;
		}
		else if(f.stat == Stats.STAT_INT)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "ИНТ" : "INT";
			return str;
		}
		else if(f.stat == Stats.STAT_WIT)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "МДР" : "WIT";
			return str;
		}
		else if(f.stat == Stats.STAT_MEN)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "ДУХ" : "MEN";
			return str;
		}
		else if(f.stat == Stats.MP_PHYSICAL_SKILL_CONSUME)
		{
			str = player.getLanguage() == Language.RUSSIAN ? "потребление мп физических скилов" : "mp consume physical skill";
			return str;
		}
		return new CustomMessage("common.not.recognized", player).toString();
	}

	private String getBodyPart(Player player, ItemTemplate item)
	{
		if(item.getBodyPart() == ItemTemplate.SLOT_R_EAR || item.getBodyPart() == ItemTemplate.SLOT_L_EAR)
			return new CustomMessage("common.item.template.name.1", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_NECK)
			return new CustomMessage("common.item.template.name.2", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_R_FINGER || item.getBodyPart() == ItemTemplate.SLOT_L_FINGER)
			return new CustomMessage("common.item.template.name.3", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_HEAD)
			return new CustomMessage("common.item.template.name.4", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_L_HAND)
			return new CustomMessage("common.item.template.name.5", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_R_HAND || item.getBodyPart() == ItemTemplate.SLOT_LR_HAND)
			return new CustomMessage("common.item.template.name.6", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_GLOVES)
			return new CustomMessage("common.item.template.name.7", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_CHEST)
			return new CustomMessage("common.item.template.name.8", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_LEGS)
			return new CustomMessage("common.item.template.name.9", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_FEET)
			return new CustomMessage("common.item.template.name.10", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_BACK)
			return new CustomMessage("common.item.template.name.11", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
			return new CustomMessage("common.item.template.name.12", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_HAIR)
			return new CustomMessage("common.item.template.name.13", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_FORMAL_WEAR)
			return new CustomMessage("common.item.template.name.14", player).toString();
		else if(item.getBodyPart() == ItemTemplate.SLOT_FORMAL_WEAR)
			return new CustomMessage("common.item.template.name.15", player).toString();
		else if(item.isUnderwear())
			return new CustomMessage("common.item.template.name.16", player).toString();
		else if(item.isBracelet())
			return new CustomMessage("common.item.template.name.17", player).toString();
		else if(item.isTalisman())
			return new CustomMessage("common.item.template.name.18", player).toString();
		else if(item.isBelt())
			return new CustomMessage("common.item.template.name.19", player).toString();
		return new CustomMessage("common.not.recognized", player).toString();
	}

	private String Button(Player player, String name, String bypass, int value)
	{
		StringBuilder result = new StringBuilder();

		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<center><button value=\"");
		result.append(name);
		result.append("\" action=\"bypass ").append(bypass).append(" ").append(value).append("\" width=200 height=29 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></center>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");

		return result.toString();
	}

	private String InfoButton(Player player, String bypass, int value)
	{
		StringBuilder result = new StringBuilder();

		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<center><br><br><button value=\"");
		result.append(new CustomMessage("communityboard.wiki.items.info", player).toString());
		result.append("\" action=\"bypass ").append(bypass).append(" ").append(value).append("\" width=200 height=29  back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"></center>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");

		return result.toString();
	}

	private String AttributeHtml(Player player, String icon, String name, int value)
	{
		StringBuilder result = new StringBuilder();

		result.append("<center><table width=690>");
		result.append("<tr>");
		result.append("<td WIDTH=690 align=center valign=top>");
		result.append("<table border=0 cellspacing=4 cellpadding=3>");
		result.append("<tr>");
		result.append("<td FIXWIDTH=50 align=right valign=top>");
		result.append("<img src=\"icon." + icon + "\" width=32 height=32>");
		result.append("</td>");
		result.append("<td FIXWIDTH=671 align=left valign=top>");
		result.append("<font color=\"0099FF\">" + name + "</font><br1><font color=\"LEVEL\">" + new CustomMessage("common.element.bonus", player).toString() + "</font> " + value);
		result.append("</td>");
		result.append("</tr>");
		result.append("</table>");
		result.append("<table border=0 cellspacing=0 cellpadding=0>");
		result.append("<tr>");
		result.append("<td width=690>");
		result.append("<img src=\"l2ui.squaregray\" width=\"690\" height=\"1\">");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table>");
		result.append("</td>");
		result.append("</tr>");
		result.append("</table></center>");

		return result.toString();
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}