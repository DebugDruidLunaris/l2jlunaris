package jts.gameserver.skills.effects;

import jts.gameserver.model.Effect;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.stats.Env;
import jts.gameserver.stats.Stats;

public class EffectManaHeal extends Effect
{
	private final boolean _ignoreMpEff;

	public EffectManaHeal(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
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

		if(_effected.isHealBlocked())
			return;

		double mp = calc();
		double newMp = Math.min(mp * 1.7, mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100., _effector, getSkill()) : 100.) / 100.);
		double addToMp = Math.max(0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100. - _effected.getCurrentMp()));

		_effected.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(addToMp)));

		if(addToMp > 0)
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}