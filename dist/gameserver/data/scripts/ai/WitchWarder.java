package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;

public class WitchWarder extends Fighter
{
	private long _wait_timeout = 0;
	private boolean _wait = false;
	private static final int DESPAWN_TIME = 3 * 60 * 1000; // 3 min

	public WitchWarder(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		if(_def_think)
		{
			doTask();
			_wait = false;
			return true;
		}

		if(!_wait)
		{
			_wait = true;
			_wait_timeout = System.currentTimeMillis() + DESPAWN_TIME;
		}

		if(_wait_timeout != 0 && _wait && _wait_timeout < System.currentTimeMillis())
			actor.deleteMe();

		return super.thinkActive();
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}