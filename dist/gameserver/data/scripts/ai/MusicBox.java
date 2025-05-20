package ai;

import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.PlaySound;

public class MusicBox extends CharacterAI
{
	public MusicBox(NpcInstance actor)
	{
		super(actor);
		ThreadPoolManager.getInstance().schedule(new ScheduleMusic(), 1000);
	}

	private class ScheduleMusic implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = (NpcInstance) getActor();
			for(Player player : World.getAroundPlayers(actor, 5000, 5000))
				player.broadcastPacket(new PlaySound("TP04_F"));
		}
	}
}