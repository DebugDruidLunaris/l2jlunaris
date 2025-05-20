package ai.hellbound;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class TorturedNative extends Fighter
{
	public TorturedNative(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		if(Rnd.chance(1))
			if(Rnd.chance(10))
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.TorturedNative.1");
			else
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.TorturedNative.2");

		return super.thinkActive();
	}
}