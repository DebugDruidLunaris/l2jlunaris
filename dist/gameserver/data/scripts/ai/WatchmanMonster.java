package ai;

import jts.commons.lang.reference.HardReference;
import jts.commons.lang.reference.HardReferences;
import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class WatchmanMonster extends Fighter
{
	private long _lastSearch = 0;
	private boolean isSearching = false;
	private HardReference<? extends Creature> _attackerRef = HardReferences.emptyRef();
	static final String[] flood = { "scripts.ai.WatchmanMonster.1", "scripts.ai.WatchmanMonster.2" };
	static final String[] flood2 = { "scripts.ai.WatchmanMonster.3", "scripts.ai.WatchmanMonster.4" };

	public WatchmanMonster(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(final Creature attacker, int damage)
	{
		final NpcInstance actor = getActor();
		if(attacker != null && !actor.getFaction().isNone() && actor.getCurrentHpPercents() < 50 && _lastSearch < System.currentTimeMillis() - 15000)
		{
			_lastSearch = System.currentTimeMillis();
			_attackerRef = attacker.getRef();

			if(findHelp())
				return;
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.WatchmanMonster.5");
		}
		super.onEvtAttacked(attacker, damage);
	}

	private boolean findHelp()
	{
		isSearching = false;
		final NpcInstance actor = getActor();
		Creature attacker = _attackerRef.get();
		if(attacker == null)
			return false;

		for(final NpcInstance npc : actor.getAroundNpc(1000, 150))
			if(!actor.isDead() && npc.isInFaction(actor) && !npc.isInCombat())
			{
				clearTasks();
				isSearching = true;
				addTaskMove(npc.getLoc(), true);
				Functions.npcSayCustomMessage(getActor(), flood[Rnd.get(flood.length)]);
				return true;
			}
		return false;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_lastSearch = 0;
		_attackerRef = HardReferences.emptyRef();
		isSearching = false;
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtArrived()
	{
		if(isSearching)
		{
			Creature attacker = _attackerRef.get();
			if(attacker != null)
			{
				Functions.npcSayCustomMessage(getActor(), flood2[Rnd.get(flood2.length)]);
				notifyFriends(attacker, 100);
			}
			isSearching = false;
			notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
		}
		else
			super.onEvtArrived();
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if(!isSearching)
			super.onEvtAggression(target, aggro);
	}
}