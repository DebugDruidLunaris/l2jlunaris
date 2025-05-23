package jts.gameserver.stats.conditions;

import jts.gameserver.stats.Env;

public class ConditionPlayerMaxLevel extends Condition
{
	private final int _level;

	public ConditionPlayerMaxLevel(int level)
	{
		_level = level;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getLevel() <= _level;
	}
}