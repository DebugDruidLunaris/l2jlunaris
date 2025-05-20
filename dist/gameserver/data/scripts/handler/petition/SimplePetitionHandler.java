package handler.petition;

import jts.gameserver.handler.petition.IPetitionHandler;
import jts.gameserver.model.Player;

public class SimplePetitionHandler implements IPetitionHandler
{
	public SimplePetitionHandler() {}

	@Override
	public void handle(Player player, int id, String txt)
	{
		player.sendMessage(txt);
	}
}