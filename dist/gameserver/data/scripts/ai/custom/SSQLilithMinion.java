package ai.custom;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;

import org.apache.commons.lang3.ArrayUtils;

public class SSQLilithMinion extends Fighter
{
	private final int[] _enemies = { 32719, 32720, 32721 };

	public SSQLilithMinion(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new Attack(), 3000);
	}

	public class Attack extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(getEnemy() != null)
				getActor().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEnemy(), 10000000);
		}
	}

	private NpcInstance getEnemy()
	{
		List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance npc : around)
				if(ArrayUtils.contains(_enemies, npc.getNpcId()))
					return npc;
		return null;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}