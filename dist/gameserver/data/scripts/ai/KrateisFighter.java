package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.KrateisCubeEvent;
import jts.gameserver.model.entity.events.objects.KrateisCubePlayerObject;
import jts.gameserver.model.instances.NpcInstance;

public class KrateisFighter extends Fighter
{
	public KrateisFighter(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);

		Player player = killer.getPlayer();
		if(player == null)
			return;

		KrateisCubeEvent cubeEvent = getActor().getEvent(KrateisCubeEvent.class);
		if(cubeEvent == null)
			return;

		KrateisCubePlayerObject particlePlayer = cubeEvent.getParticlePlayer(player);

		particlePlayer.setPoints(particlePlayer.getPoints() + 3);
		cubeEvent.updatePoints(particlePlayer);
	}
}