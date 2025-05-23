package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public class EffectConsumeSoulsOverTime extends Effect
{
	public EffectConsumeSoulsOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;

		if(_effected.getConsumedSouls() < 0)
			return false;

		int damage = (int) calc();

		if(_effected.getConsumedSouls() < damage)
			_effected.setConsumedSouls(0, null);
		else
			_effected.setConsumedSouls(_effected.getConsumedSouls() - damage, null);

		return true;
	}
}