package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class FrightenedOrc extends Fighter
{
	private boolean _sayOnAttack;

	public FrightenedOrc(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_sayOnAttack = true;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(attacker != null && Rnd.chance(10) && _sayOnAttack)
		{
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.FrightenedOrc");
			_sayOnAttack = false;
		}

		super.onEvtAttacked(attacker, damage);
	}
}