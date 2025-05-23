package ai.isle_of_prayer;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class DarkWaterDragon extends Fighter
{
	private int _mobsSpawned = 0;
	private static final int FAFURION = 18482;
	private static final int SHADE1 = 22268;
	private static final int SHADE2 = 22269;
	private static final int MOBS[] = { SHADE1, SHADE2 };
	private static final int MOBS_COUNT = 5;
	private static final int RED_CRYSTAL = 9596;

	public DarkWaterDragon(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead())
			switch(_mobsSpawned)
			{
				case 0:
					_mobsSpawned = 1;
					spawnShades(attacker);
					break;
				case 1:
					if(actor.getCurrentHp() < actor.getMaxHp() / 2)
					{
						_mobsSpawned = 2;
						spawnShades(attacker);
					}
					break;
			}

		super.onEvtAttacked(attacker, damage);
	}

	private void spawnShades(Creature attacker)
	{
		NpcInstance actor = getActor();
		for(int i = 0; i < MOBS_COUNT; i++)
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(MOBS[Rnd.get(MOBS.length)]));
				sp.setLoc(Location.findPointToStay(actor, 100, 120));
				NpcInstance npc = sp.doSpawn(true);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_mobsSpawned = 0;
		NpcInstance actor = getActor();
		try
		{
			SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(FAFURION));
			sp.setLoc(Location.findPointToStay(actor, 100, 120));
			sp.doSpawn(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(killer != null)
		{
			final Player player = killer.getPlayer();
			if(player != null)
				if(Rnd.chance(77))
					actor.dropItem(player, RED_CRYSTAL, 1);
		}
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return _mobsSpawned == 0;
	}
}