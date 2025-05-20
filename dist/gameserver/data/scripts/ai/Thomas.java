package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class Thomas extends Fighter
{
	private long _lastSay;

	private static final String[] _stay = 
	{
		"scripts.ai.Thomas.1",
		"scripts.ai.Thomas.2",
		"scripts.ai.Thomas.3",
		"scripts.ai.Thomas.4" 
	};

	private static final String[] _attacked = 
	{
		"scripts.ai.Thomas.5",
		"scripts.ai.Thomas.6",
		"scripts.ai.Thomas.7",
		"scripts.ai.Thomas.8",
		"scripts.ai.Thomas.9",
		"scripts.ai.Thomas.10",
		"scripts.ai.Thomas.11" 
	};

	public Thomas(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		// Ругаемся не чаще, чем раз в 10 секунд
		if(!actor.isInCombat() && System.currentTimeMillis() - _lastSay > 10000)
		{
			Functions.npcSayCustomMessage(getActor(), _stay[Rnd.get(_stay.length)]);
			_lastSay = System.currentTimeMillis();
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if(attacker == null || attacker.getPlayer() == null)
			return;

		// Ругаемся не чаще, чем раз в 5 секунд
		if(System.currentTimeMillis() - _lastSay > 5000)
		{
			Functions.npcSayCustomMessage(getActor(), _attacked[Rnd.get(_attacked.length)]);
			_lastSay = System.currentTimeMillis();
		}
		super.onEvtAttacked(attacker, damage);
	}
}