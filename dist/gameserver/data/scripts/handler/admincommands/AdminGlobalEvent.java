package handler.admincommands;

import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class AdminGlobalEvent extends ScriptAdminCommand
{
	enum Commands
	{
		admin_list_events
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands c = (Commands) comm;
		switch(c)
		{
			case admin_list_events:
				GameObject object = activeChar.getTarget();
				if(object == null)
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				else
					for(GlobalEvent e : object.getEvents())
						activeChar.sendMessage("- " + e.toString());
				break;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}