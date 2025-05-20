package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public class EffectInterrupt extends Effect
{
	public EffectInterrupt(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getEffected().isRaid())
			getEffected().abortCast(false, true);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}