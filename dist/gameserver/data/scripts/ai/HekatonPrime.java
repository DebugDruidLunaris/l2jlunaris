package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class HekatonPrime extends Fighter
{
	private long _lastTimeAttacked;

	public HekatonPrime(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		_lastTimeAttacked = System.currentTimeMillis();
	}

	@Override
	protected boolean thinkActive()
	{
		if(_lastTimeAttacked + 600000 < System.currentTimeMillis())
		{
			if(getActor().getMinionList().hasMinions())
				getActor().getMinionList().deleteMinions();
			getActor().deleteMe();
			return true;
		}
		return false;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		_lastTimeAttacked = System.currentTimeMillis();
		super.onEvtAttacked(attacker, damage);
	}
}