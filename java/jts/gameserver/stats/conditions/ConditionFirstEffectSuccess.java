package jts.gameserver.stats.conditions;

import jts.gameserver.stats.Env;

public class ConditionFirstEffectSuccess extends Condition
{
	boolean _param;

	public ConditionFirstEffectSuccess(boolean param)
	{
		_param = param;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return _param == (env.value == Integer.MAX_VALUE);
	}
}