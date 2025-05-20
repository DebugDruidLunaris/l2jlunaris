package jts.gameserver.model.instances;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.entity.events.objects.TerritoryWardObject;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class TerritoryWardInstance extends NpcInstance
{
	private final TerritoryWardObject _territoryWard;

	public TerritoryWardInstance(int objectId, NpcTemplate template, TerritoryWardObject territoryWardObject)
	{
		super(objectId, template);
		setHasChatWindow(false);
		_territoryWard = territoryWardObject;
	}

	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(_territoryWard.canPickUp(player))
		{
			_territoryWard.pickUp(player);
			decayMe();
		}
	}

	@Override
	protected void onDecay()
	{
		decayMe();

		_spawnAnimation = 2;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return false;
		DominionSiegeEvent siegeEvent2 = attacker.getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null)
			return false;
		if(siegeEvent == siegeEvent2)
			return false;
		if(siegeEvent2.getResidence().getOwner() != attacker.getClan())
			return false;
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}