package jts.gameserver.handler.voicecommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;

public class Autocp extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "autocp" };

	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(command.equals("autocp"))
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("ACPhold"))
					{
						int var = NumberUtils.toInt(param[1], 99);

						if(var > 99)
							var = 99;
						else if(var < 1)
							var = 1;

						player.setVar("ACPhold", String.valueOf(var), -1);
						if(player.stateAutoCPUse() != null)
						{
							player.endAutoCPUse();
							player.runAutoCPUse();
						}
					}
				}
			}

		String dialog = HtmCache.getInstance().getNotNull("command/autocp.htm", player);	
		dialog = dialog.replaceFirst("%ACPhold%", player.getVarB("ACPhold") ? "" + player.getVar("ACPhold") + "" : "99");
		show(dialog, player);
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}