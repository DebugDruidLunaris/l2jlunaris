package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class Quest421FairyTree extends Fighter
{
	public Quest421FairyTree(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(attacker == null)
			return;

		NpcInstance actor = getActor();
		if(attacker.isPlayer())
		{
			Skill skill = SkillTable.getInstance().getInfo(5423, 12);
			skill.getEffects(actor, attacker, false, false);
			return;
		}
		if(attacker.isPet())
		{
			super.onEvtAttacked(attacker, damage);
			return;
		}
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		NpcInstance actor = getActor();
		if(attacker != null && attacker.isPlayer())
		{
			Skill skill = SkillTable.getInstance().getInfo(5423, 12);
			skill.getEffects(actor, attacker, false, false);
			return;
		}
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}