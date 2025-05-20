package ai.custom;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class SSQLilimServantMage extends Mystic
{
	private boolean _attacked = false;

	public SSQLilimServantMage(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		super.onEvtAttacked(attacker, damage);
		if(Rnd.chance(30) && !_attacked)
		{
			Functions.npcSay(getActor(), "Кто смеет войти без очереди?");
			_attacked = true;
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(30))
			Functions.npcSay(getActor(), "Господь Шилен ... когда-нибудь ... вы будете выполнять ... эту миссию ...");
		super.onEvtDead(killer);
	}
}