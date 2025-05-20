package jts.gameserver.model.instances.residences.dominion;

import jts.commons.geometry.Circle;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Territory;
import jts.gameserver.model.World;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.events.impl.DominionSiegeEvent;
import jts.gameserver.model.instances.residences.SiegeFlagInstance;
import jts.gameserver.stats.Stats;
import jts.gameserver.stats.funcs.FuncMul;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.templates.ZoneTemplate;
import jts.gameserver.templates.npc.NpcTemplate;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public class OutpostInstance extends SiegeFlagInstance
{
	private class OnZoneEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature actor)
		{
			DominionSiegeEvent siegeEvent = OutpostInstance.this.getEvent(DominionSiegeEvent.class);
			if(siegeEvent == null)
				return;

			if(actor.getEvent(DominionSiegeEvent.class) != siegeEvent)
				return;

			actor.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 0x40, OutpostInstance.this, 2.));
			actor.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 0x40, OutpostInstance.this, 2.));
			actor.addStatFunc(new FuncMul(Stats.REGENERATE_CP_RATE, 0x40, OutpostInstance.this, 2.));
		}

		@Override
		public void onZoneLeave(Zone zone, Creature actor)
		{
			actor.removeStatsOwner(OutpostInstance.this);
		}
	}

	private Zone _zone = null;

	public OutpostInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		Circle c = new Circle(getLoc(), 250);
		c.setZmax(World.MAP_MAX_Z);
		c.setZmin(World.MAP_MIN_Z);

		StatsSet set = new StatsSet();
		set.set("name", StringUtils.EMPTY);
		set.set("type", Zone.ZoneType.dummy);
		set.set("territory", new Territory().add(c));

		_zone = new Zone(new ZoneTemplate(set));
		_zone.setReflection(ReflectionManager.DEFAULT);
		_zone.addListener(new OnZoneEnterLeaveListenerImpl());
		_zone.setActive(true);
	}

	@Override
	public void onDelete()
	{
		super.onDelete();

		_zone.setActive(false);
		_zone = null;
	}

	@Override
	public boolean isInvul()
	{
		return true;
	}
}