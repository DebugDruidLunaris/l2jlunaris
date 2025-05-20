package ai.hellbound;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class OriginalSinWarden extends Fighter
{
	private static final int[] servants1 = { 22424, 22425, 22426, 22427, 22428, 22429, 22430 };
	private static final int[] servants2 = { 22432, 22433, 22434, 22435, 22436, 22437, 22438 };
	private static final int[] DarionsFaithfulServants = { 22405, 22406, 22407 };

	public OriginalSinWarden(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		switch(actor.getNpcId())
		{
			case 22423:
			{
				for(int i = 0; i < servants1.length; i++)
					try
					{
						SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(servants1[i]));
						sp.setLoc(Location.findPointToStay(actor, 150, 350));
						sp.doSpawn(true);
						sp.stopRespawn();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				break;
			}
			case 22431:
			{
				for(int i = 0; i < servants2.length; i++)
					try
					{
						SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(servants2[i]));
						sp.setLoc(Location.findPointToStay(actor, 150, 350));
						sp.doSpawn(true);
						sp.stopRespawn();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				break;
			}
			default:
				break;
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		if(Rnd.chance(15))
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(DarionsFaithfulServants[Rnd.get(DarionsFaithfulServants.length - 1)]));
				sp.setLoc(Location.findPointToStay(actor, 150, 350));
				sp.doSpawn(true);
				sp.stopRespawn();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		super.onEvtDead(killer);
	}
}