package ai;

import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Mystic;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Playable;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class GatekeeperZombie extends Mystic
{
	public GatekeeperZombie(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return false;
		if(target.isAlikeDead() || !target.isPlayable())
			return false;
		if(!target.isInRangeZ(actor.getSpawnedLoc(), actor.getAggroRange()))
			return false;
		if(Functions.getItemCount((Playable) target, 8067) != 0 || Functions.getItemCount((Playable) target, 8064) != 0)
			return false;
		if(!GeoEngine.canSeeTarget(actor, target, false))
			return false;

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			actor.getAggroList().addDamageHate(target, 0, 1);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}

		return true;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}