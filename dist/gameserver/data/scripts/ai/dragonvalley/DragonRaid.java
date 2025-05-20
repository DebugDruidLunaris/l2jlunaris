package ai.dragonvalley;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.Creature;

public class DragonRaid extends Fighter
{
	private long _lastHit;
	private NpcInstance boss = getActor();

	public DragonRaid(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		if(_lastHit + 1500000 < System.currentTimeMillis())
		{
			boss.deleteMe();
			return false;
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtSpawn()
	{
		_lastHit = System.currentTimeMillis();
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		_lastHit = System.currentTimeMillis();
		super.onEvtAttacked(attacker, damage);
	}
}