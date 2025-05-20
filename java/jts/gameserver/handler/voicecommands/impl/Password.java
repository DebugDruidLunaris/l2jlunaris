package jts.gameserver.handler.voicecommands.impl;


import jts.gameserver.Config;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.utils.Util;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.ChangePassword;


public class Password extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList =
	{
		"password"
	};
	
	public void check(String[] var)
	{
		Player self = getSelf();
		if (var.length != 3)
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectValues", self, new Object[0]), self);
			return;
		}
		useVoicedCommand("password", self, var[0] + " " + var[1] + " " + var[2]);
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if ((command.equals("password")) && ((args == null) || (args.equals(""))))
	    {
	      String dialog = "";
	      if (Config.SERVICES_CHANGE_PASSWORD)
	        dialog = HtmCache.getInstance().getNotNull("command/password.htm", player);
	      else
	        dialog = HtmCache.getInstance().getNotNull("command/nopassword.htm", player);
	      show(dialog, player);
	      return true;
	    }
	
		if (Config.PASSWORD_PAY_ID > 0)
		{
			if (player.getInventory().getCountOf(Config.PASSWORD_PAY_ID) < Config.PASSWORD_PAY_COUNT)
			{
				if (player.isLangRus())
				{
					player.sendMessage("Для того что-бы сменить пароль вам нужно заплатить " + Config.PASSWORD_PAY_COUNT + " " + Config.PASSWORD_PAY_ID + "");
				}
				else
				{
					player.sendMessage("In order to change password you must pay " + Config.PASSWORD_PAY_COUNT + " " + Config.PASSWORD_PAY_ID + "");
				}
				return false;
			}
		}
		
		String[] parts = args.split(" ");
		
		if (parts.length != 3)
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectValues", player, new Object[0]), player);
			return false;
		}
		
		if (!parts[1].equals(parts[2]))
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectConfirmation", player, new Object[0]), player);
			return false;
		}
		
		if (parts[1].equals(parts[0]))
		{
			show(new CustomMessage("scripts.commands.user.password.NewPassIsOldPass", player, new Object[0]), player);
			return false;
		}
		
		if ((parts[1].length() < 5) || (parts[1].length() > 20))
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectSize", player, new Object[0]), player);
			return false;
		}
		
		if (!Util.isMatchingRegexp(parts[1], Config.APASSWD_TEMPLATE))
		{
			show(new CustomMessage("scripts.commands.user.password.IncorrectInput", player, new Object[0]), player);
			return false;
		}
		
		LoginServerCommunication.getInstance().sendPacket(new ChangePassword(player.getAccountName(), parts[0], parts[1]));

		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return this._commandList;
	}
}