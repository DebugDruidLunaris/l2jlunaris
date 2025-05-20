package ai;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.PositionUtils;

public class LizardmanSummoner extends Mystic
{
	private final int TANTA_LIZARDMAN_SCOUT = 22768;
	private final int SPAWN_COUNT = 2;
	private boolean spawnedMobs = false;

	public LizardmanSummoner(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		spawnedMobs = false;
		super.onEvtSpawn();
	}

	@Override
	@SuppressWarnings("unused")
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(!spawnedMobs && attacker.isPlayable())
		{
			NpcInstance actor = getActor();
			for(int i = 0; i < SPAWN_COUNT; i++)
				try
				{
					SimpleSpawner sp = new SimpleSpawner(TANTA_LIZARDMAN_SCOUT);
					int radius = (i % 2 == 0 ? -1 : 1) * 16000;
					int x = (int) (actor.getX() + 80 * Math.cos(actor.headingToRadians(actor.getHeading() - 32768 + radius)));
					int y = (int) (actor.getY() + 80 * Math.sin(actor.headingToRadians(actor.getHeading() - 32768 + radius)));
					sp.setLoc(actor.getLoc());
					NpcInstance npc = sp.doSpawn(true);
					npc.setHeading(PositionUtils.calculateHeadingFrom(npc, attacker));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			spawnedMobs = true;
		}
		super.onEvtAttacked(attacker, damage);
	}
}