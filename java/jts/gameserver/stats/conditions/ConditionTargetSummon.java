package jts.gameserver.stats.conditions;

import jts.gameserver.model.Creature;
import jts.gameserver.stats.Env;

public class ConditionTargetSummon extends Condition
{
	private final boolean _flag;

	public ConditionTargetSummon(boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		return target != null && target.isSummon() == _flag;
	}
}