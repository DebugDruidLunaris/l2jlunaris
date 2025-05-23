package jts.gameserver.stats.conditions;

import jts.gameserver.model.Player;
import jts.gameserver.stats.Env;

public class ConditionPlayerAgathion extends Condition
{
	private final int _agathionId;

	public ConditionPlayerAgathion(int agathionId)
	{
		_agathionId = agathionId;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		if(((Player) env.character).getAgathionId() > 0 && _agathionId == -1)
			return true;
		return ((Player) env.character).getAgathionId() == _agathionId;
	}
}