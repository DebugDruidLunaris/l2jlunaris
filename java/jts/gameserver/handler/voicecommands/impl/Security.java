package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.ChangeAllowedHwid;
import jts.gameserver.loginservercon.gspackets.ChangeAllowedIp;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;

public class Security extends Functions implements IVoicedCommandHandler
{

	private String[] _commandList = { "lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equalsIgnoreCase("lock"))
		{
			String html = HtmCache.getInstance().getNotNull("/mods/lock/lock.htm", activeChar);
			html = html.replace("%ip_block%", IpBlockStatus(activeChar));
			html = html.replace("%hwid_block%", HwidBlockStatus(activeChar));
			html = html.replace("%hwid_val%", HwidBlockBy());
			html = html.replace("%curIP%", activeChar.getIP());
			show(html, activeChar);
			return true;
		}

		else if(command.equalsIgnoreCase("lockIp"))
		{
			if(!Config.ALLOW_IP_LOCK)
			{
				String html = HtmCache.getInstance().getNotNull("/mods/lock/lock_disable.htm", activeChar);
				show(html, activeChar);
				return true;
			}

			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));

			String html = HtmCache.getInstance().getNotNull("/mods/lock/lock_ip.htm", activeChar);
			html = html.replace("%curIP%", activeChar.getIP());
			show(html, activeChar);
			return true;
		}

		else if(command.equalsIgnoreCase("lockHwid"))
		{
			if(!Config.ALLOW_HWID_LOCK)
			{
				String html = HtmCache.getInstance().getNotNull("/mods/lock/lock_disable.htm", activeChar);
				show(html, activeChar);
				return true;
			}

			String html = HtmCache.getInstance().getNotNull("/mods/lock/lock_hwid.htm", activeChar);
			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getNetConnection().getHWID()));
			show(html, activeChar);
			return true;
		}

		else if(command.equalsIgnoreCase("unlockIp"))
		{
			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), ""));

			String html = HtmCache.getInstance().getNotNull("/mods/lock/unlock_ip.htm", activeChar);
			show(html, activeChar);
			return true;
		}

		else if(command.equalsIgnoreCase("unlockHwid"))
		{
			LoginServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), ""));

			String html = HtmCache.getInstance().getNotNull("/mods/lock/unlock_hwid.htm", activeChar);
			show(html, activeChar);
			return true;
		}

		return true;
	}

	private String IpBlockStatus(Player activeChar)
	{
		if(Config.ALLOW_IP_LOCK)
			return activeChar.isLangRus() ? "Разрешено" : "Allowed";
		else
			return activeChar.isLangRus() ? "Запрещено" : "Not allowed";
	}

	private String HwidBlockStatus(Player activeChar)
	{
		if(Config.ALLOW_HWID_LOCK)
			return activeChar.isLangRus() ? "Разрешено" : "Allowed";
		return activeChar.isLangRus() ? "Запрещено" : "Not allowed";
	}

	private String HwidBlockBy()
	{
		String result = "(CPU/HDD)";

		switch(Config.HWID_LOCK_MASK)
		{
			case 2:
				result = "(HDD)";
				break;
			case 4:
				result = "(BIOS)";
				break;
			case 6:
				result = "(BIOS/HDD)";
				break;
			case 8:
				result = "(CPU)";
				break;
			case 10:
				result = "(CPU/HDD)";
				break;
			case 12:
				result = "(CPU/BIOS)";
				break;
			case 14:
				result = "(CPU/HDD/BIOS)";
				break;
			case 1:
			case 3:
			case 5:
			case 7:
			case 9:
			case 11:
			case 13:
			default:
				result = "(unknown)";

		}
		return result;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}