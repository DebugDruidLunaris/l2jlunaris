package jts.gameserver.handler.voicecommands.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

public class ServerInfo extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "date", "time" };

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		String title = new CustomMessage("common.voicecommand", player).toString();

		if(command.equals("time"))
		{
			StringBuilder html = new StringBuilder("<html noscrollbar><title>" + title + "</title><body><table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td valign=\"top\" align=\"center\">");
			html.append("<center><br><br><font name=\"hs12\">" + new CustomMessage("common.voicecommand.info", player).addString(".time") + "</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><font color=\"B59A75\" name=\"hs12\">" + new CustomMessage("common.voicecommand.time", player) + "</font><font name=\"hs12\">" + TIME_FORMAT.format(new Date(System.currentTimeMillis())) + "</font>");
			html.append("</center></td></tr></table></body></html>");

			show(html.toString(), player);
			return true;
		}

		if(command.equals("date"))
		{
			StringBuilder html = new StringBuilder("<html noscrollbar><title>" + title + "</title><body><table border=0 cellpadding=0 cellspacing=0 width=292 height=358 background=\"l2ui_ct1.Windows_DF_TooltipBG\"><tr><td valign=\"top\" align=\"center\">");
			html.append("<center><br><br><font name=\"hs12\">" + new CustomMessage("common.voicecommand.info", player).addString(".date") + "</font><br1><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><font color=\"B59A75\" name=\"hs12\">" + new CustomMessage("common.voicecommand.date", player) + "</font><font name=\"hs12\">" + DATE_FORMAT.format(new Date(System.currentTimeMillis())) + "</font>");
			html.append("</center></td></tr></table></body></html>");

			show(html.toString(), player);
			return true;
		}

		return false;
	}
}