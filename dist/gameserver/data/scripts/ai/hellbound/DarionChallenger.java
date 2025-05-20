package ai.hellbound;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class DarionChallenger extends Fighter
{
	private static final int TeleportCube = 32467;

	public DarionChallenger(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(checkAllDestroyed())
			try
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(TeleportCube));
				sp.setLoc(new Location(-12527, 279714, -11622, 16384));
				sp.doSpawn(true);
				sp.stopRespawn();
				ThreadPoolManager.getInstance().schedule(new Unspawn(), 600 * 1000L); // 10 mins
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		super.onEvtDead(killer);
	}

	private static boolean checkAllDestroyed()
	{
		if(!GameObjectsStorage.getAllByNpcId(25600, true).isEmpty())
			return false;
		if(!GameObjectsStorage.getAllByNpcId(25601, true).isEmpty())
			return false;
		if(!GameObjectsStorage.getAllByNpcId(25602, true).isEmpty())
			return false;

		return true;
	}

	private class Unspawn extends RunnableImpl
	{
		public Unspawn()
		{}

		@Override
		public void runImpl()
		{
			for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(TeleportCube, true))
				npc.deleteMe();
		}
	}
}