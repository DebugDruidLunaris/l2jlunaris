package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public class EffectCPDamPercent extends Effect
{
	public EffectCPDamPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if(_effected.isDead())
			return;

		double newCp = (100. - calc()) * _effected.getMaxCp() / 100.;
		newCp = Math.min(_effected.getCurrentCp(), Math.max(0, newCp));
		_effected.setCurrentCp(newCp);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}