package jts.gameserver.handler.bypass;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;

public interface IBypassHandler
{
	String[] getBypasses();
	void onBypassFeedback(NpcInstance npc, Player player, String command);
}