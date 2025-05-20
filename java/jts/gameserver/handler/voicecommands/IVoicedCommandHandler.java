package jts.gameserver.handler.voicecommands;

import jts.gameserver.model.Player;

public interface IVoicedCommandHandler
{
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param activeChar
	 * @param command
	 * @return command success
	 */
	public boolean useVoicedCommand(String command, Player activeChar, String target);

	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public String[] getVoicedCommandList();
}