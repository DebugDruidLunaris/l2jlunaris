package handler.bypass;

import jts.gameserver.handler.bypass.BypassHandler;
import jts.gameserver.handler.bypass.IBypassHandler;
import jts.gameserver.scripts.ScriptFile;

public abstract class ScriptBypassHandler implements ScriptFile, IBypassHandler
{
	@Override
	public void onLoad()
	{
		BypassHandler.getInstance().registerBypass(this);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}