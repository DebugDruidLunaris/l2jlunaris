package jts.gameserver.network.clientpackets;

import jts.gameserver.handler.usercommands.IUserCommandHandler;
import jts.gameserver.handler.usercommands.UserCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.utils.Log_New;

public class BypassUserCmd extends L2GameClientPacket
{
	private int _command;

	@Override
	protected void readImpl()
	{
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);

		if(handler == null)
			activeChar.sendMessage(new CustomMessage("common.S1NotImplemented", activeChar).addString(String.valueOf(_command)));
		else
			handler.useUserCommand(_command, activeChar);
		Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "UserCommands", new String[] { "player used user command: " + this._command + "" });
	}
}