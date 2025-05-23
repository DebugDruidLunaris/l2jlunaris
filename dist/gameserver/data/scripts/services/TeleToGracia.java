package services;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class TeleToGracia extends Functions
{
	public void tele()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();

		if(player != null && npc != null)
			if(player.getLevel() < 75)
				show("teleporter/" + npc.getNpcId() + "-4.htm", player);
			else if(player.getAdena() >= 150000)
			{
				player.reduceAdena(150000, true);
				player.teleToLocation(-149406, 255247, -80);
			}
			else
				show("teleporter/" + npc.getNpcId() + "-2.htm", player);
	}
}