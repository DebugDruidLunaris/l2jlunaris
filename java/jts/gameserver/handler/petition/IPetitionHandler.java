package jts.gameserver.handler.petition;

import jts.gameserver.model.Player;

public interface IPetitionHandler
{
	void handle(Player player, int id, String txt);
}