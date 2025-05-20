package services.community;

import jts.gameserver.Config;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.scripts.ScriptFile;
public class CommunityBoardVote implements ScriptFile, ICommunityBoardHandler
{

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsvote" };
	}

	@Override
	public void onBypassCommand(Player player, String command)
	{
		//TODO
		/*if(command.equals("_bbsvote"))
		else if(command.startsWith("_bbssmscounrty"))
		{}
		else */if(player.isLangRus())
			ShowBoard.separateAndSend("<html><body><br><br><center>На данный момент функция: " + command + " пока не реализована</center><br><br>", player);
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>At the moment the function: " + command + " not implemented yet</center><br><br></body></html>", player);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}
}