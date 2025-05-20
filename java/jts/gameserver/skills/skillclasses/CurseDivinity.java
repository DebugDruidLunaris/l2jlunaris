package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Skill;
import jts.gameserver.stats.Formulas;
import jts.gameserver.templates.StatsSet;

public class CurseDivinity extends Skill
{
	public CurseDivinity(StatsSet set)
	{
		super(set);
		_power = set.getInteger("power", 1);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : activeChar.getChargedSoulShot() ? 2 : 0) : 0;

		Creature realTarget;
		boolean reflected;

		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;

				if(!target.isPlayer())
					continue;
				
				reflected = target.checkReflectSkill(activeChar, this);
				realTarget = reflected ? activeChar : target;

				List<Effect> effectsList = target.getEffectList().getAllEffects();
				int buffCount = effectsList.size();
				double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
				if(damage >= 1)
				{
					damage = damage + (_power * 0.1 + _power * 0.254 * buffCount);
					realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
				}
					

				getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}

		if(isSuicideAttack())
			activeChar.doDie(null);
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}