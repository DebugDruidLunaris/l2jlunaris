package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public class EffectNegateEffects extends Effect
{
	public EffectNegateEffects(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onExit()
	{
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		for(Effect e : _effected.getEffectList().getAllEffects())
			if(!e.getStackType().equals(EffectTemplate.NO_STACK) && (e.getStackType().equals(getStackType()) || e.getStackType().equals(getStackType2())) || !e.getStackType2().equals(EffectTemplate.NO_STACK) && (e.getStackType2().equals(getStackType()) || e.getStackType2().equals(getStackType2())))
				if(e.getStackOrder() <= getStackOrder())
					e.exit();
		return false;
	}
}