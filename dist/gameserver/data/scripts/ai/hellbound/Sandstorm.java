package ai.hellbound;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;

public class Sandstorm extends DefaultAI
{
	private static final int AGGRO_RANGE = 200;
	private static final Skill SKILL1 = SkillTable.getInstance().getInfo(5435, 1);
	private static final Skill SKILL2 = SkillTable.getInstance().getInfo(5494, 1);
	private long lastThrow = 0;

	public Sandstorm(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(lastThrow + 5000 < System.currentTimeMillis())
			for(Playable target : World.getAroundPlayables(actor, AGGRO_RANGE, AGGRO_RANGE))
				if(target != null && !target.isAlikeDead() && !target.isInvul() && target.isVisible() && GeoEngine.canSeeTarget(actor, target, false))
				{
					actor.doCast(SKILL1, target, true);
					actor.doCast(SKILL2, target, true);
					lastThrow = System.currentTimeMillis();
					break;
				}

		return super.thinkActive();
	}

	@Override
	protected void thinkAttack() {}

	@Override
	protected void onIntentionAttack(Creature target) {}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage) {}

	@Override
	protected boolean randomWalk()
	{
		NpcInstance actor = getActor();
		Location sloc = actor.getSpawnedLoc();

		Location pos = Location.findPointToStay(actor, sloc, 150, 300);
		if(GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), pos.x, pos.y, pos.z, actor.getGeoIndex()))
		{
			actor.setRunning();
			addTaskMove(pos, false);
		}

		return true;
	}
}