package jts.gameserver.handler.voicecommands.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jts.commons.text.PrintfFormat;
import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

import org.apache.commons.lang3.math.NumberUtils;

public class Cfg extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "lang", "cfg" };
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yy HH:mm");

	public static final PrintfFormat cfg_row = new PrintfFormat("<table><tr><td width=5></td><td width=120><font color=\"FF6600\">%s:</td><td width=100>%s</td></tr></table>");
	public static final PrintfFormat cfg_empty = new PrintfFormat("<table><tr><td width=5></td><td width=260><center> %s </center></td></tr></table>");
	public static final PrintfFormat cfg_button = new PrintfFormat("<button width=%d height=17 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\" action=\"bypass -h user_cfg %s\" value=\"%s\">");

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(command.equals("cfg"))
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("dli"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("DroplistIcons", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("DroplistIcons");

					if(param[0].equalsIgnoreCase("lang"))
						if(param[1].equalsIgnoreCase("en"))
							activeChar.setVar("lang@", "en", -1);
						else if(param[1].equalsIgnoreCase("ru"))
							activeChar.setVar("lang@", "ru", -1);

					if(param[0].equalsIgnoreCase("noe"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("NoExp", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("NoExp");
					if(param[0].equalsIgnoreCase(Player.NO_TRADERS_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowTraders(true);
							activeChar.setVar(Player.NO_TRADERS_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("off"))
						{
							activeChar.setNotShowTraders(false);
							activeChar.unsetVar(Player.NO_TRADERS_VAR);
						}

					if(param[0].equalsIgnoreCase(Player.NO_ANIMATION_OF_CAST_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowBuffAnim(false);
							activeChar.setVar(Player.NO_ANIMATION_OF_CAST_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("off"))
						{
							activeChar.setNotShowBuffAnim(true);
							activeChar.unsetVar(Player.NO_ANIMATION_OF_CAST_VAR);
						}

					if(param[0].equalsIgnoreCase("noShift"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noShift", "1", -1);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("noShift");
					
					if(param[0].equalsIgnoreCase("skill_chance"))
					{
						if(param[1].equalsIgnoreCase("on") && Config.SKILL_CHANCE_ENABLE)
							activeChar.setVar("SkillsHideChance", "1", -1L);
						else if(param[1].equalsIgnoreCase("off"))
							activeChar.unsetVar("SkillsHideChance");
					}
					
					if(Config.SERVICES_ENABLE_NO_CARRIER && param[0].equalsIgnoreCase("noCarrier"))
					{
						int time = NumberUtils.toInt(param[1], Config.SERVICES_NO_CARRIER_DEFAULT_TIME);
						if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
							time = Config.SERVICES_NO_CARRIER_MAX_TIME;
						else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
							time = Config.SERVICES_NO_CARRIER_MIN_TIME;
						activeChar.setVar("noCarrier", String.valueOf(time), -1);
					}

					if(param[0].equalsIgnoreCase("autoloot"))
						activeChar.setAutoLoot(Boolean.parseBoolean(param[1]));

					if(param[0].equalsIgnoreCase("autolooth"))
						activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
				}
			}

		String dialog = HtmCache.getInstance().getNotNull("command/cfg.htm", activeChar);

		dialog = dialog.replaceFirst("%lang%", activeChar.getVar("lang@").toUpperCase());
		if(activeChar.getVar("lang@").equals("ru"))
		{
		dialog = dialog.replaceFirst("%dli%", activeChar.getVarB("DroplistIcons") ? "Вкл" : "Выкл");
		}
		if(activeChar.getVar("lang@").equals("en"))
		{
			dialog = dialog.replaceFirst("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
		}
		if(activeChar.getVar("lang@").equals("ru"))
		{
		dialog = dialog.replaceFirst("%noe%", activeChar.getVarB("NoExp") ? "Вкл" : "Выкл");
		}
		if(activeChar.getVar("lang@").equals("en"))
		{
		dialog = dialog.replaceFirst("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
		}
		if(activeChar.getVar("lang@").equals("ru"))
		{
		dialog = dialog.replaceFirst("%notraders%", activeChar.getVarB("notraders") ? "Вкл" : "Выкл");
		}
		if(activeChar.getVar("lang@").equals("en"))
		{
		dialog = dialog.replaceFirst("%notraders%", activeChar.getVarB("notraders") ? "On" : "Off");
		}
		if(activeChar.getVar("lang@").equals("ru"))
		{
		dialog = dialog.replaceFirst("%notShowBuffAnim%", activeChar.getVarB("notShowBuffAnim") ? "Вкл" : "Выкл");
		}
		if(activeChar.getVar("lang@").equals("en"))
		{
		dialog = dialog.replaceFirst("%notShowBuffAnim%", activeChar.getVarB("notShowBuffAnim") ? "On" : "Off");
		}
		if(activeChar.getVar("lang@").equals("ru"))
		{
		dialog = dialog.replaceFirst("%noShift%", activeChar.getVarB("noShift") ? "Вкл" : "Выкл");
		}
		if(activeChar.getVar("lang@").equals("en"))
		{
		dialog = dialog.replaceFirst("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");
		}
		dialog = dialog.replaceFirst("%noCarrier%", Config.SERVICES_ENABLE_NO_CARRIER ? activeChar.getVarB("noCarrier") ? activeChar.getVar("noCarrier") : "0" : "N/A");
		if(!Config.SKILL_CHANCE_ENABLE)
		{
			dialog = dialog.replaceFirst("%skill_chance%", "<font color=\"LEVEL\">N/A</font>");
		}
		else
		{
			if(!activeChar.getVarB("SkillsHideChance"))
				if(activeChar.getVar("lang@").equals("ru"))
				{
				dialog = dialog.replaceFirst("%skill_chance%", "Выкл");
				}
				if(activeChar.getVar("lang@").equals("en"))
				{
					dialog = dialog.replaceFirst("%skill_chance%", "Off");
				}
			else
				if(activeChar.getVar("lang@").equals("ru"))
				{
				dialog = dialog.replaceFirst("%skill_chance%", "Вкл");
				}
				if(activeChar.getVar("lang@").equals("en"))
				{
					dialog = dialog.replaceFirst("%skill_chance%", "On");
				}

	        if(activeChar.getNetConnection().getBonus() > 1)
	        {
	            long endtime = activeChar.getNetConnection().getBonusExpire();
	            if(endtime > 0)
	                dialog = dialog.replaceFirst("%endtime%", DATE_FORMAT.format(new Date(endtime * 1000L)));
	        }
	        else			
			if(activeChar.getVar("lang@").equals("ru"))
			{
	            dialog = dialog.replaceFirst("%endtime%", "<a action=\"bypass -h scripts_services.RateBonus:list2\"><font color=\"LEVEL\">Купить ПА</a>");
			}
			if(activeChar.getVar("lang@").equals("en"))
			{
	            dialog = dialog.replaceFirst("%endtime%", "<a action=\"bypass -h scripts_services.RateBonus:list2\"><font color=\"LEVEL\">Buy Premium</a>");
			}
		}
	
		String additional = "";
		String additional2 = "";

		if(Config.ALT_AUTO_LOOT_INDIVIDUAL)
		{
			String bt;
			if(activeChar.isAutoLootEnabled())
				bt = cfg_button.sprintf(new Object[] { 100, "autoloot false", new CustomMessage("common.Disable", activeChar).toString() });
			else
				bt = cfg_button.sprintf(new Object[] { 100, "autoloot true", new CustomMessage("common.Enable", activeChar).toString() });
			if(activeChar.getVar("lang@").equals("ru"))
			{
			additional += cfg_row.sprintf(new Object[] { "Авто-Лут", bt });
			}
			if(activeChar.getVar("lang@").equals("en"))
			{
			additional += cfg_row.sprintf(new Object[] { "AutoLoot", bt });
			}
			if(activeChar.isAutoLootHerbsEnabled())
				bt = cfg_button.sprintf(new Object[] { 100, "autolooth false", new CustomMessage("common.Disable", activeChar).toString() });
			else
				bt = cfg_button.sprintf(new Object[] { 100, "autolooth true", new CustomMessage("common.Enable", activeChar).toString() });
			if(activeChar.getVar("lang@").equals("ru"))
			{
			additional2 += cfg_row.sprintf(new Object[] {  "Авто-Лут Хербов", bt });
			}
			if(activeChar.getVar("lang@").equals("en"))
			{
			additional2 += cfg_row.sprintf(new Object[] { "AutoLoot Herbs", bt });
			}
			}
		else
		{
			if(activeChar.getVar("lang@").equals("ru"))
			{
				additional += cfg_empty.sprintf(new Object[] { "Сервис автоподбора отключен." });
				additional2 += cfg_empty.sprintf(new Object[] { "Сервис автоподбора отключен." });

			}
			if(activeChar.getVar("lang@").equals("en"))
			{
				additional += cfg_empty.sprintf(new Object[] { "Services autoloot is disable." });
				additional2 += cfg_empty.sprintf(new Object[] { "Services autoloot is disable." });

			}
		}
		dialog = dialog.replaceFirst("%additional2%", additional2);

		StringBuilder events2 = new StringBuilder();
		for(GlobalEvent e : activeChar.getEvents())
			events2.append(e.toString()).append("<br>");
		dialog = dialog.replace("%events2%", events2.toString());
		
		dialog = dialog.replaceFirst("%additional%", additional);
		StringBuilder events = new StringBuilder();
		for(GlobalEvent e : activeChar.getEvents())
			events.append(e.toString()).append("<br>");
		dialog = dialog.replace("%events%", events.toString());

		show(dialog, activeChar);

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}