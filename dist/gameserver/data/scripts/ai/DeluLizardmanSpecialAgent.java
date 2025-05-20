package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Ranger;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class DeluLizardmanSpecialAgent extends Ranger
{
	private boolean _firstTimeAttacked = true;

	public DeluLizardmanSpecialAgent(NpcInstance actor)
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
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.DeluLizardmanSpecialAgent.1");
		}
		else if(Rnd.chance(10))
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.DeluLizardmanSpecialAgent.2");
		super.onEvtAttacked(attacker, damage);
	}
}