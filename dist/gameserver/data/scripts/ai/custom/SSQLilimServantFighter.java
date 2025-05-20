package ai.custom;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class SSQLilimServantFighter extends Fighter
{
	private boolean _attacked = false;

	public SSQLilimServantFighter(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(Rnd.chance(30) && !_attacked)
		{
			Functions.npcSay(getActor(), Rnd.chance(50) ? "Те, кто боятся должны уйти и тем, кто храбр должны бороться!" : "Это место принадлежал Лорду Шилен.");
			_attacked = true;
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(30))
			Functions.npcSay(getActor(), Rnd.chance(50) ? "Почему вы на нашем пути?" : "Шилен ... наш Шилен!");
		super.onEvtDead(killer);
	}
}