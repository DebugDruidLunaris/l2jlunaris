package ai.hellbound;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.instancemanager.naia.NaiaCoreManager;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.ReflectionUtils;

public class NaiaCube extends DefaultAI
{

	public NaiaCube(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new Despawn(getActor()), 120 * 1000L);
	}

	private class Despawn extends RunnableImpl
	{
		NpcInstance _npc;

		private Despawn(NpcInstance npc)
		{
			_npc = npc;
		}

		@Override
		public void runImpl()
		{
			_npc.deleteMe();
			NaiaCoreManager.setZoneActive(false);
			ReflectionUtils.getDoor(20240001).openMe(); // Beleth Door
			ReflectionUtils.getDoor(18250025).openMe(); // Epidos Door
		}
	}
}