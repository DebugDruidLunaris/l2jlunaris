package ai;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class PowderKeg extends DefaultAI
{
	public PowderKeg(NpcInstance actor)
	{
		super(actor);
	}

	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		{
			actor.doCast(SkillTable.getInstance().getInfo(5714, 1), attacker, true);
			for(Creature c : actor.getAroundCharacters(600, 200))
				c.reduceCurrentHp(1700, actor, null, true, true, false, false, false, false, false);
			actor.doDie(attacker);
		}
		super.onEvtAttacked(attacker, damage);
	}
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
}