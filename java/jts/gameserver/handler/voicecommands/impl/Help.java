package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.base.Experience;
import jts.gameserver.network.serverpackets.RadarControl;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

public class Help extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "help", "exp", "whereis" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("help"))
			return help(command, activeChar, args);
		if(command.equalsIgnoreCase("whereis"))
			return whereis(command, activeChar, args);
		if(command.equalsIgnoreCase("exp"))
			return exp(command, activeChar, args);

		return false;
	}

	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isSubClassActive() ? Experience.getMaxSubLevel() : Experience.getMaxLevel()))
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel", activeChar));
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft", activeChar).addNumber(exp));
		}
		return true;
	}

	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = World.getPlayer(args);
		if(friend == null)
			return false;

		if(friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			RadarControl rc = new RadarControl(0, 1, friend.getLoc());
			activeChar.sendPacket(rc);
			return true;
		}

		return false;
	}

	private boolean help(String command, Player activeChar, String args)
	{
		String dialog = HtmCache.getInstance().getNotNull("command/help.htm", activeChar);
		show(dialog, activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}