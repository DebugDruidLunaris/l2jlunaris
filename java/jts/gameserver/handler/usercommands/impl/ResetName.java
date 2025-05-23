package jts.gameserver.handler.usercommands.impl;

import jts.gameserver.handler.usercommands.IUserCommandHandler;
import jts.gameserver.model.Player;

public class ResetName implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 117 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;

		if(activeChar.getVar("oldtitle") != null)
		{
			activeChar.setTitleColor(Player.DEFAULT_TITLE_COLOR);
			activeChar.setTitle(activeChar.getVar("oldtitle"));
			activeChar.broadcastUserInfo(true);
			return true;
		}
		return false;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}