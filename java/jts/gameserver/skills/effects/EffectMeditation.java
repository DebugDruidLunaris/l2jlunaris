package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public final class EffectMeditation extends Effect
{
	public EffectMeditation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startParalyzed();
		_effected.setMeditated(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopParalyzed();
		_effected.setMeditated(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}