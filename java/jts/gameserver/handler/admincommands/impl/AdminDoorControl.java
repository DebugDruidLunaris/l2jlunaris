package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.cache.Msg;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.DoorInstance;

public class AdminDoorControl implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_open,
		admin_close
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Door)
			return false;

		GameObject target;

		switch(command)
		{
			case admin_open:
				if(wordList.length > 1)
					target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
				else
					target = activeChar.getTarget();

				if(target != null && target.isDoor())
					((DoorInstance) target).openMe();
				else
					activeChar.sendPacket(Msg.INVALID_TARGET);

				break;
			case admin_close:
				if(wordList.length > 1)
					target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
				else
					target = activeChar.getTarget();
				if(target != null && target.isDoor())
					((DoorInstance) target).closeMe();
				else
					activeChar.sendPacket(Msg.INVALID_TARGET);
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