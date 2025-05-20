package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class CaughtMystic extends Mystic
{
	private static final int TIME_TO_LIVE = 60000;
	private final long TIME_TO_DIE = System.currentTimeMillis() + TIME_TO_LIVE;

	public CaughtMystic(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		if(Rnd.chance(75))
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.CaughtMob.spawn");
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(75))
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.CaughtMob.death");

		super.onEvtDead(killer);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(System.currentTimeMillis() >= TIME_TO_DIE)
		{
			actor.deleteMe();
			return false;
		}
		return super.thinkActive();
	}
}