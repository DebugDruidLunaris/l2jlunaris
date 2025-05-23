package jts.gameserver.stats.conditions;

import jts.gameserver.stats.Env;

public class ConditionPlayerMinMaxDamage extends Condition
{
	private final double _min;
	private final double _max;

	public ConditionPlayerMinMaxDamage(double min, double max)
	{
		_min = min;
		_max = max;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(_min > 0 && env.value < _min)
			return false;
		if(_max > 0 && env.value > _max)
			return false;
		return true;
	}
}