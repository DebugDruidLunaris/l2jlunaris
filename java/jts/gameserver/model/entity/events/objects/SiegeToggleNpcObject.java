package jts.gameserver.model.entity.events.objects;

import java.util.Set;

import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class SiegeToggleNpcObject implements SpawnableObject
{
	private SiegeToggleNpcInstance _toggleNpc;
	private Location _location;

	public SiegeToggleNpcObject(int id, int fakeNpcId, Location loc, int hp, Set<String> set)
	{
		_location = loc;

		_toggleNpc = (SiegeToggleNpcInstance) NpcHolder.getInstance().getTemplate(id).getNewInstance();

		_toggleNpc.initFake(fakeNpcId);
		_toggleNpc.setMaxHp(hp);
		_toggleNpc.setZoneList(set);
	}

	@Override
	public void spawnObject(GlobalEvent event)
	{
		_toggleNpc.decayFake();

		if(event.isInProgress())
			_toggleNpc.addEvent(event);
		else
			_toggleNpc.removeEvent(event);

		_toggleNpc.setCurrentHp(_toggleNpc.getMaxHp(), true);
		_toggleNpc.spawnMe(_location);
	}

	@Override
	public void despawnObject(GlobalEvent event)
	{
		_toggleNpc.removeEvent(event);
		_toggleNpc.decayFake();
		_toggleNpc.decayMe();
	}

	@Override
	public void refreshObject(GlobalEvent event) {}

	public SiegeToggleNpcInstance getToggleNpc()
	{
		return _toggleNpc;
	}

	public boolean isAlive()
	{
		return _toggleNpc.isVisible();
	}
}