package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.instancemanager.HellboundManager;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;

public class Hellbound extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "hellbound" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equals("hellbound"))
		{
			activeChar.sendMessage("Hellbound level: " + HellboundManager.getHellboundLevel());
			activeChar.sendMessage("Confidence: " + HellboundManager.getConfidence());
		}
		return false;
	}
}