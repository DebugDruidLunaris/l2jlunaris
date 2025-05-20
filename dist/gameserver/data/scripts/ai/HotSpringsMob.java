package ai;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class HotSpringsMob extends Mystic
{
	private static final int DeBuffs[] = { 4554, 4552 };

	public HotSpringsMob(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker != null && Rnd.chance(5))
		{
			int DeBuff = DeBuffs[Rnd.get(DeBuffs.length)];
			List<Effect> effect = attacker.getEffectList().getEffectsBySkillId(DeBuff);
			if(effect != null)
			{
				int level = effect.get(0).getSkill().getLevel();
				if(level < 10)
				{
					effect.get(0).exit();
					Skill skill = SkillTable.getInstance().getInfo(DeBuff, level + 1);
					skill.getEffects(actor, attacker, false, false);
				}
			}
			else
			{
				Skill skill = SkillTable.getInstance().getInfo(DeBuff, 1);
				if(skill != null)
					skill.getEffects(actor, attacker, false, false);
				else
					System.out.println("Skill " + DeBuff + " is null, fix it.");
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}