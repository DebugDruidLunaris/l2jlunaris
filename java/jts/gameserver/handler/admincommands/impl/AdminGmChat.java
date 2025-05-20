package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.tables.GmListTable;

public class AdminGmChat implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_gmchat,
		admin_snoop,
		admin_unsnoop
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanAnnounce)
			return false;

		switch(command)
		{
			case admin_gmchat:
				try
				{
					String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
					Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
					GmListTable.broadcastToGMs(cs);
				}
				catch(StringIndexOutOfBoundsException e)
				{}
				break;
			case admin_snoop:
			{
				GameObject target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage("Вы должны выбрать цель..");
					return false;
				}
				if(!target.isPlayer())
				{
					activeChar.sendMessage("Цель должна быть Игроком.");
					return false;
				}
				Player player = (Player) target;
				player.addSnooper(activeChar);
				activeChar.addSnooped(player);
				activeChar.sendMessage("Прослушивание Игрока: " + target.getName()+" Включено");
				break;
			}
			case admin_unsnoop:
			{
				GameObject target = activeChar.getTarget();
				if(target == null)
				{
					activeChar.sendMessage("Вы должны выбрать цель.");
					return false;
				}
				if(!target.isPlayer())
				{
					activeChar.sendMessage("Цель должна быть Игроком.");
					return false;
				}
				Player player = (Player) target;
				activeChar.removeSnooped(player);
				activeChar.sendMessage("Прослушивание Игрока: " + target.getName()+" Выключено");
				break;
			}
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