package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.model.Player;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminHellbound implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_hbadd,
		admin_hbsub,
		admin_hbset
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Menu)
			return false;

		switch(command)
		{
			case admin_hbadd:
				HellboundManager.addConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage("Added " + NumberUtils.toInt(wordList[1], 1) + " to Hellbound confidence");
				activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
				break;
			case admin_hbsub:
				HellboundManager.reduceConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage("Reduced confidence by " + NumberUtils.toInt(wordList[1], 1));
				activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
				break;
			case admin_hbset:
				HellboundManager.setConfidence(Long.parseLong(wordList[1]));
				activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
				break;
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}