package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.Player;

public class AdminRide implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_ride,
		admin_ride_wyvern,
		admin_ride_strider,
		admin_ride_red_strider,
		admin_ride_guardian_strider,
		admin_ride_fenrir,
		admin_ride_snow_fenrir,
		admin_ride_lion,
		admin_ride_horse,
		admin_ride_beetle,
		admin_unride
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().Rider)
			return false;

		Object target = activeChar.getTarget();

		if(target == null || !((Player) target).isPlayer())
			target = activeChar;

		switch(command)
		{
			case admin_ride:
				if(activeChar.isMounted() || activeChar.getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Incorrect id.");
					return false;
				}
				activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85);
				break;
			case admin_ride_wyvern:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(12621, 0, 85);
				break;
			case admin_ride_fenrir:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(16037, 0, 85);
				break;
			case admin_ride_snow_fenrir:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(16042, 0, 85);
				break;
			case admin_ride_strider:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(12526, 0, 85);
				break;
			case admin_ride_red_strider:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(16038, 0, 85);
				break;
			case admin_ride_guardian_strider:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(16068, 0, 85);
				break;
			case admin_ride_lion:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(13146, 0, 85);
				break;
			case admin_ride_horse:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(13130, 0, 85);
				break;
			case admin_ride_beetle:
				if(((Player) target).isMounted() || ((Player) target).getPet() != null)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Target Have a Pet or Mounted." : "У цели вызван питомец или находится верхом.");
					return false;
				}
				((Player) target).setMount(13147, 0, 85);
				break;
			case admin_unride:
				((Player) target).setMount(0, 0, 0);
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