package ai.custom;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class GvGBoss extends Fighter
{
	boolean phrase1 = false;
	boolean phrase2 = false;
	boolean phrase3 = false;

	public GvGBoss(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		if(actor.getCurrentHpPercents() < 50 && phrase1 == false)
		{
			phrase1 = true;
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.GvGBoss.1");
		}
		else if(actor.getCurrentHpPercents() < 30 && phrase2 == false)
		{
			phrase2 = true;
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.GvGBoss.2");
		}
		else if(actor.getCurrentHpPercents() < 5 && phrase3 == false)
		{
			phrase3 = true;
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.GvGBoss.3");
		}

		super.onEvtAttacked(attacker, damage);
	}
}