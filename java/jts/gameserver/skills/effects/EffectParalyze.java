package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public final class EffectParalyze extends Effect
{
	public EffectParalyze(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isParalyzeImmune())
			return false;
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startParalyzed();
		_effected.abortAttack(true, true);
		_effected.abortCast(true, true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopParalyzed();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}