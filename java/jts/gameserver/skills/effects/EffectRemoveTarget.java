package jts.gameserver.skills.effects;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Effect;
import jts.gameserver.stats.Env;

public final class EffectRemoveTarget extends Effect
{
	private boolean _doStopTarget;

	public EffectRemoveTarget(Env env, EffectTemplate template)
	{
		super(env, template);
		_doStopTarget = template.getParam().getBool("doStopTarget", false);
	}

	@Override
	public boolean checkCondition()
	{
		return Rnd.chance(_template.chance(80));//на РПГ пообще в чате пишет что шанс 55% оставим 80%
	}

	@Override
	public void onStart()
	{
		if(getEffected().getAI() instanceof DefaultAI)
			((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);

		getEffected().setTarget(null);
		if(_doStopTarget)
			getEffected().stopMove();
		getEffected().abortAttack(true, true);
		getEffected().abortCast(true, true);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}