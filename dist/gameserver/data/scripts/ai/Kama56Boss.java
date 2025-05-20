package ai;

import java.util.List;

import jts.commons.collections.LazyArrayList;
import jts.commons.lang.reference.HardReference;
import jts.commons.lang.reference.HardReferences;
import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.MinionList;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.MinionInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;

public class Kama56Boss extends Fighter
{
	private long _nextOrderTime = 0;
	private HardReference<Player> _lastMinionsTargetRef = HardReferences.emptyRef();

	public Kama56Boss(NpcInstance actor)
	{
		super(actor);
	}

	private void sendOrderToMinions(NpcInstance actor)
	{
		if(!actor.isInCombat())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		MinionList ml = actor.getMinionList();
		if(ml == null || !ml.hasMinions())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		long now = System.currentTimeMillis();
		if(_nextOrderTime > now && _lastMinionsTargetRef.get() != null)
		{
			Player old_target = _lastMinionsTargetRef.get();
			if(old_target != null && !old_target.isAlikeDead())
			{
				for(MinionInstance m : ml.getAliveMinions())
					if(m.getAI().getAttackTarget() != old_target)
						m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, old_target, 10000000);
				return;
			}
		}

		_nextOrderTime = now + 30000;

		List<Player> pl = World.getAroundPlayers(actor);
		if(pl.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		List<Player> alive = new LazyArrayList<Player>();
		for(Player p : pl)
			if(!p.isAlikeDead())
				alive.add(p);
		if(alive.isEmpty())
		{
			_lastMinionsTargetRef = HardReferences.emptyRef();
			return;
		}

		Player target = alive.get(Rnd.get(alive.size()));
		_lastMinionsTargetRef = target.getRef();

		Functions.npcSayCustomMessage(actor, "Kama56Boss.attack", target.getName());
		for(MinionInstance m : ml.getAliveMinions())
		{
			m.getAggroList().clear();
			m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 10000000);
		}
	}

	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		sendOrderToMinions(actor);
		super.thinkAttack();
	}
}