package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Ranger;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class KarulBugbear extends Ranger
{
	private boolean _firstTimeAttacked = true;

	public KarulBugbear(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_firstTimeAttacked = true;
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
				Functions.npcSayCustomMessage(actor, "scripts.ai.KarulBugbear.1");
		}
		else if(Rnd.chance(10))
			Functions.npcSayCustomMessage(actor, "scripts.ai.KarulBugbear.2");
		super.onEvtAttacked(attacker, damage);
	}
}