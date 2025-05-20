package ai;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SocialAction;
import jts.gameserver.scripts.Functions;

public class GeneralDilios extends DefaultAI
{
	private static final int GUARD_ID = 32619;
	private long _wait_timeout = 0;

	private static final String[] diliosText = 
	{
		/* "Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Infinity is currently secured under the flag of the Keucereus Alliance!", */
		"scripts.ai.GeneralDilios.1",
		"scripts.ai.GeneralDilios.2",
		"scripts.ai.GeneralDilios.3" 
	};

	public GeneralDilios(NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 10000;
	}

	@Override
	public boolean thinkActive()
	{
		NpcInstance actor = getActor();

		if(System.currentTimeMillis() > _wait_timeout)
		{
			_wait_timeout = System.currentTimeMillis() + 60000;
			int j = Rnd.get(1, 3);
			switch(j)
			{
				case 1:
					Functions.npcSayCustomMessage(getActor(), diliosText[0]);
					break;
				case 2:
					Functions.npcSayCustomMessage(getActor(), diliosText[1]);
					break;
				case 3:
					Functions.npcSayCustomMessage(getActor(), diliosText[2]);
					List<NpcInstance> around = actor.getAroundNpc(1500, 100);
					if(around != null && !around.isEmpty())
						for(NpcInstance guard : around)
							if(!guard.isMonster() && guard.getNpcId() == GUARD_ID)
								guard.broadcastPacket(new SocialAction(guard.getObjectId(), 4));
			}
		}
		return false;
	}
}