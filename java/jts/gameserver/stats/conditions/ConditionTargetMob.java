package jts.gameserver.stats.conditions;

import jts.gameserver.stats.Env;

public class ConditionTargetMob extends Condition
{
	private final boolean _isMob;

	public ConditionTargetMob(boolean isMob)
	{
		_isMob = isMob;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.target != null && env.target.isMonster() == _isMob;
	}
}