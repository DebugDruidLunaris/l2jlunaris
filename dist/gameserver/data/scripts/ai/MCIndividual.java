package ai;

import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SocialAction;

public class MCIndividual extends DefaultAI
{
	public MCIndividual(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		ThreadPoolManager.getInstance().schedule(new ScheduleSocial(), 1000);
		super.onEvtSpawn();
	}

	private class ScheduleSocial implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			actor.broadcastPacket(new SocialAction(actor.getObjectId(), 1));
		}
	}
}