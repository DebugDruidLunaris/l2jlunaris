package handler.usercommands;

import jts.gameserver.handler.usercommands.IUserCommandHandler;
import jts.gameserver.handler.usercommands.UserCommandHandler;
import jts.gameserver.scripts.ScriptFile;

public abstract class ScriptUserCommand implements IUserCommandHandler, ScriptFile
{
	@Override
	public void onLoad()
	{
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}