package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public final class EffectHealBlock extends Effect
{
	public EffectHealBlock(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startHealBlocked();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopHealBlocked();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}