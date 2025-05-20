package ai.hellbound;

import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class Pylon extends Fighter
{
	public Pylon(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		for(int i = 0; i < 7; i++)
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(22422));
				sp.setLoc(Location.findPointToStay(actor, 150, 550));
				sp.doSpawn(true);
				sp.stopRespawn();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
}