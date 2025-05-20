package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.Player;

public class AdminGm implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gm
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		// TODO зачем отключено?
		if(Boolean.TRUE)
			return false;

		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;

		switch(command)
		{
			case admin_gm:
				handleGm(activeChar);
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

	private void handleGm(Player activeChar)
	{
		if(activeChar.isGM())
		{
			activeChar.getPlayerAccess().IsGM = false;
			activeChar.sendMessage("You no longer have GM status.");
		}
		else
		{
			activeChar.getPlayerAccess().IsGM = true;
			activeChar.sendMessage("You have GM status now.");
		}
	}
}