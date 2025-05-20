package jts.gameserver.network.clientpackets;

import jts.gameserver.handler.admincommands.AdminCommandHandler;
import jts.gameserver.model.Player;

public class SendBypassBuildCmd extends L2GameClientPacket
{
	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();

		if(_command != null)
			_command = _command.trim();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		String cmd = _command;

		if(!cmd.contains("admin_"))
			cmd = "admin_" + cmd;

		AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, cmd);
	}
}