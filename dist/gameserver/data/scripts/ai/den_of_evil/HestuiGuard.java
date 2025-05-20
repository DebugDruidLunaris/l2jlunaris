package ai.den_of_evil;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.taskmanager.AiTaskManager;

public class HestuiGuard extends DefaultAI
{
	public HestuiGuard(NpcInstance actor)
	{
		super(actor);

	}

	@Override
	public synchronized void startAITask()
	{
		if(_aiTask == null)
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);
	}

	@Override
	protected synchronized void switchAITask(long NEW_DELAY) {}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();

		for(Player player : World.getAroundPlayers(actor))
		{
			if(player.getLevel() <= 37)
				Functions.npcSay(actor, NpcString.THIS_PLACE_IS_DANGEROUS_S1, player.getName());
		}

		return false;
	}
}