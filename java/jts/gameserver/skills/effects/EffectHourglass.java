package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public final class EffectHourglass extends Effect
{
	public EffectHourglass(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayer())
			_effected.getPlayer().startHourglassEffect();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayer())
			_effected.getPlayer().stopHourglassEffect();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}