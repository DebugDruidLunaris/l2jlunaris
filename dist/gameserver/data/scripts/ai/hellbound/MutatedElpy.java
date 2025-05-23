package ai.hellbound;

import jts.gameserver.ai.Fighter;
import jts.gameserver.instancemanager.naia.NaiaCoreManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class MutatedElpy extends Fighter
{
	public MutatedElpy(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NaiaCoreManager.launchNaiaCore();
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		actor.doDie(attacker);
	}
}