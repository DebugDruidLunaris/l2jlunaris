package jts.gameserver.stats.conditions;

import jts.gameserver.model.entity.Reflection;
import jts.gameserver.stats.Env;

public class ConditionPlayerInstanceZone extends Condition
{
	private final int _id;

	public ConditionPlayerInstanceZone(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Reflection ref = env.character.getReflection();

		return ref.getInstancedZoneId() == _id;
	}
}