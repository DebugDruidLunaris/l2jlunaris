package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;

public class Gargos extends Fighter
{
	private long _lastFire;

	public Gargos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		return super.thinkActive() || thinkFire();
	}

	protected boolean thinkFire()
	{
		if(System.currentTimeMillis() - _lastFire > 60000L)
		{
			NpcInstance actor = getActor();
			Functions.npcSayCustomMessage(actor, "scripts.ai.Gargos.fire");
			actor.doCast(SkillTable.getInstance().getInfo(5705, 1), actor, false);
			_lastFire = System.currentTimeMillis();
			return true;
		}

		return false;
	}
}