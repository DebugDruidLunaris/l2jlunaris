package jts.gameserver.stats.conditions;

import jts.gameserver.model.Player;
import jts.gameserver.stats.Env;

public class ConditionPlayerRiding extends Condition
{
	public enum CheckPlayerRiding
	{
		NONE,
		STRIDER,
		WYVERN
	}

	private final CheckPlayerRiding _riding;

	public ConditionPlayerRiding(CheckPlayerRiding riding)
	{
		_riding = riding;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
			return false;
		if(_riding == CheckPlayerRiding.STRIDER && ((Player) env.character).isRiding())
			return true;
		if(_riding == CheckPlayerRiding.WYVERN && ((Player) env.character).isFlying())
			return true;
		if(_riding == CheckPlayerRiding.NONE && !((Player) env.character).isRiding() && !((Player) env.character).isFlying())
			return true;
		return false;
	}
}