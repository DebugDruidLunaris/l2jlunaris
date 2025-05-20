package ai;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class DeluLizardmanSpecialCommander extends Fighter
{
	private boolean _shouted = false;

	public DeluLizardmanSpecialCommander(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_shouted = false;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(Rnd.chance(40) && !_shouted)
		{
			_shouted = true;
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.DeluLizardmanSpecialCommander");

			List<NpcInstance> around = actor.getAroundNpc(1000, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.isMonster())
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
		}
		super.onEvtAttacked(attacker, damage);
	}
}