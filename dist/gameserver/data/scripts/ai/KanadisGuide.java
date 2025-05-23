package ai;

import java.util.List;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class KanadisGuide extends Fighter
{

	public KanadisGuide(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		List<NpcInstance> around = actor.getAroundNpc(5000, 300);
		if(around != null && !around.isEmpty())
			for(NpcInstance npc : around)
				if(npc.getNpcId() == 36562)
					actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 5000);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker.getNpcId() == 36562)
		{
			actor.getAggroList().addDamageHate(attacker, 0, 1);
			startRunningTask(2000);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		return false;
	}
}