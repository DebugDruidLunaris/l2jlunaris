package ai;

import java.util.List;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Priest;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class RagnaHealer extends Priest
{
	private long lastFactionNotifyTime;

	public RagnaHealer(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
			return;

		if(System.currentTimeMillis() - lastFactionNotifyTime > 10000)
		{
			lastFactionNotifyTime = System.currentTimeMillis();
			List<NpcInstance> around = actor.getAroundNpc(500, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.isMonster() && npc.getNpcId() >= 22691 && npc.getNpcId() <= 22702)
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
		}

		super.onEvtAttacked(attacker, damage);
	}
}