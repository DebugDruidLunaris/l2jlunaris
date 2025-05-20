package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class OlMahumGeneral extends Fighter
{
	private boolean _firstTimeAttacked = true;

	public OlMahumGeneral(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.OlMahumGeneral.1");
		}
		else if(Rnd.chance(10))
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.OlMahumGeneral.2");
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}