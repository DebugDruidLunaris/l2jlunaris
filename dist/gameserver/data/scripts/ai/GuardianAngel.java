package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class GuardianAngel extends DefaultAI
{
	static final String[] flood = 
	{
		"scripts.ai.GuardianAngel.1",
		"scripts.ai.GuardianAngel.2",
		"scripts.ai.GuardianAngel.3" 
	};

	public GuardianAngel(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		Functions.npcSayCustomMessage(getActor(), flood[Rnd.get(2)]);

		return super.thinkActive();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(actor != null)
			Functions.npcSay(actor, flood[2]);
		super.onEvtDead(killer);
	}
}