package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;

public class SealDevice extends Fighter
{
	private boolean _firstAttack = false;

	public SealDevice(NpcInstance actor)
	{
		super(actor);
		actor.block();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!_firstAttack)
		{
			actor.broadcastPacket(new MagicSkillUse(actor, actor, 5980, 1, 0, 0));
			_firstAttack = true;
		}
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro) {}
}