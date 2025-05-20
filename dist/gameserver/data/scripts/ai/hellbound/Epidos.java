package ai.hellbound;

import jts.gameserver.ai.Fighter;
import jts.gameserver.instancemanager.naia.NaiaCoreManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class Epidos extends Fighter
{

	public Epidos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NaiaCoreManager.removeSporesAndSpawnCube();
		super.onEvtDead(killer);
	}
}