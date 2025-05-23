package jts.gameserver.stats.triggers;

import jts.commons.lang.ArrayUtils;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.skills.AddedSkill;
import jts.gameserver.stats.Env;
import jts.gameserver.stats.conditions.Condition;

public class TriggerInfo extends AddedSkill
{
	private final TriggerType _type;
	private final double _chance;
	private Condition[] _conditions = Condition.EMPTY_ARRAY;

	public TriggerInfo(int id, int level, TriggerType type, double chance)
	{
		super(id, level);
		_type = type;
		_chance = chance;
	}

	public final void addCondition(Condition c)
	{
		_conditions = ArrayUtils.add(_conditions, c);
	}

	public boolean checkCondition(Creature actor, Creature target, Creature aimTarget, Skill owner, double damage)
	{
		// Скилл проверяется и кастуется на aimTarget
		if(getSkill().checkTarget(actor, aimTarget, aimTarget, false, false) != null)
			return false;

		Env env = new Env();
		env.character = actor;
		env.skill = owner;
		env.target = target; // В условии проверяется реальная цель.
		env.value = damage;

		for(Condition c : _conditions)
			if(!c.test(env))
				return false;
		return true;
	}

	public TriggerType getType()
	{
		return _type;
	}

	public double getChance()
	{
		return _chance;
	}
}