package jts.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Skill;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.stats.Formulas;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.FuncTemplate;
import jts.gameserver.templates.StatsSet;

public class NegateStats extends Skill
{
	private final List<Stats> _negateStats;
	private final boolean _negateOffensive;
	private final int _negateCount;

	public NegateStats(StatsSet set)
	{
		super(set);

		String[] negateStats = set.getString("negateStats", "").split(" ");
		_negateStats = new ArrayList<Stats>(negateStats.length);
		for(String stat : negateStats)
			if(!stat.isEmpty())
				_negateStats.add(Stats.valueOfXml(stat));

		_negateOffensive = set.getBool("negateDebuffs", false);
		_negateCount = set.getInteger("negateCount", 0);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(!_negateOffensive && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(getId(), getLevel()));
					continue;
				}

				int count = 0;
				List<Effect> effects = target.getEffectList().getAllEffects();
				for(Stats stat : _negateStats)
					for(Effect e : effects)
					{
						Skill skill = e.getSkill();
						// Если у бафа выше уровень чем у скилла Cancel, то есть шанс, что этот баф не снимется
						if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
						{
							count++;
							continue;
						}
						if(skill.isOffensive() == _negateOffensive && containsStat(e, stat) && skill.isCancelable())
						{
							target.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
							e.exit();
							count++;
						}
						if(_negateCount > 0 && count >= _negateCount)
							break;
					}

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private boolean containsStat(Effect e, Stats stat)
	{
		for(FuncTemplate ft : e.getTemplate().getAttachedFuncs())
			if(ft._stat == stat)
				return true;
		return false;
	}

	@Override
	public boolean isOffensive()
	{
		return !_negateOffensive;
	}

	public List<Stats> getNegateStats()
	{
		return _negateStats;
	}
}