package jts.gameserver.skills.skillclasses;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.templates.StatsSet;

import java.util.List;

public class AIeffects extends Skill
{
	public AIeffects(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
				getEffects(activeChar, target, getActivateRate() > 0, false);

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}