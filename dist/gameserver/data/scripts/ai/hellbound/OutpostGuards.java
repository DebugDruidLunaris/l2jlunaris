package ai.hellbound;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;

public class OutpostGuards extends Fighter
{
	public OutpostGuards(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
}