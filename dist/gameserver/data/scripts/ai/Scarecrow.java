package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class Scarecrow extends Fighter
{
	public Scarecrow(NpcInstance actor)
	{
		super(actor);
		actor.block();
		actor.setIsInvul(true);
	}

	@Override
	protected void onIntentionAttack(Creature target) {}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}
}