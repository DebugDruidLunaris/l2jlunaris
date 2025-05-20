package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.utils.Location;

public class LeylaDancer extends DefaultAI
{
	private static int count = 0;

	public LeylaDancer(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		ThreadPoolManager.getInstance().schedule(new ScheduleStart(), 5000);
		ThreadPoolManager.getInstance().schedule(new ScheduleMoveFinish(), 220000);
		super.onEvtSpawn();
	}

	private class ScheduleStart implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				if(count < 50)
				{
					count++;
					actor.broadcastPacket(new SocialAction(actor.getObjectId(), Rnd.get(1, 2)));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(), 3600);
				}
				else
				{
					count = 0;
				}
			}
		}
	}

	private class ScheduleMoveFinish implements Runnable
	{
		@Override
		public void run()
		{
			NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.sendPacket(new ExShowScreenMessage(NpcString.WE_LOVE_YOU, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
				addTaskMove(new Location(-56594, -56064, -1988), true);
				doTask();
			}
		}
	}
}