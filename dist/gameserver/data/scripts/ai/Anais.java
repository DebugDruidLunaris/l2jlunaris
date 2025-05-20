package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Zone;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.ReflectionUtils;

public class Anais extends Fighter
{
	private static Zone _zone;

	public Anais(NpcInstance actor)
	{
		super(actor);
		this.AI_TASK_ATTACK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 1000;
		_zone = ReflectionUtils.getZone("[FourSepulchers1]");
	}

	/* @Override
	protected boolean maybeMoveToHome()
	{
		NpcInstance actor = getActor();
		if (actor != null && !_zone.checkIfInZone(actor))
			teleportHome(true);
		return false;
	}*/

	public static Zone getZone()
	{
		return _zone;
	}

	public boolean canSeeInSilentMove(Playable target)
	{
		return (!target.isSilentMoving()) || (Rnd.chance(10));
	}
}