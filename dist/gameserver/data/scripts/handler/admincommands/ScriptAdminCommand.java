package handler.admincommands;

import jts.gameserver.handler.admincommands.AdminCommandHandler;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.scripts.ScriptFile;

public abstract class ScriptAdminCommand implements IAdminCommandHandler, ScriptFile
{
	@Override
	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}