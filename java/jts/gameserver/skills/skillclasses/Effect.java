package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.templates.StatsSet;

public class Effect extends Skill
{
	public Effect(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
				getEffects(activeChar, target, false, false);
	}
}