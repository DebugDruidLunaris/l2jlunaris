package jts.gameserver.stats.conditions;

import jts.gameserver.stats.Env;

public class ConditionPlayerPercentMp extends Condition
{
	private final double _mp;

	public ConditionPlayerPercentMp(int mp)
	{
		_mp = mp / 100.;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentMpRatio() <= _mp;
	}
}