package jts.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jts.gameserver.instancemanager.SpawnManager;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.instances.NpcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SpawnExObject implements SpawnableObject
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnExObject.class);

	private final List<Spawner> _spawns;
	private boolean _spawned;
	private String _name;

	public SpawnExObject(String name)
	{
		_name = name;
		_spawns = SpawnManager.getInstance().getSpawners(_name);
		if(_spawns.isEmpty())
			_log.info("SpawnExObject: not found spawn group: " + name);
	}

	@Override
	public void spawnObject(GlobalEvent event)
	{
		if(_spawned)
			_log.info("SpawnExObject: can't spawn twice: " + _name + "; event: " + event, new Exception());
		else
		{
			for(Spawner spawn : _spawns)
			{
				if(event.isInProgress())
					spawn.addEvent(event);
				else
					spawn.removeEvent(event);

				spawn.setReflection(event.getReflection());
				spawn.init();
			}
			_spawned = true;
		}
	}

	@Override
	public void despawnObject(GlobalEvent event)
	{
		if(!_spawned)
			return;
		_spawned = false;
		for(Spawner spawn : _spawns)
		{
			spawn.removeEvent(event);
			spawn.deleteAll();
		}
	}

	@Override
	public void refreshObject(GlobalEvent event)
	{
		for(NpcInstance npc : getAllSpawned())
			if(event.isInProgress())
				npc.addEvent(event);
			else
				npc.removeEvent(event);
	}

	public List<Spawner> getSpawns()
	{
		return _spawns;
	}

	public List<NpcInstance> getAllSpawned()
	{
		List<NpcInstance> npcs = new ArrayList<NpcInstance>();
		for(Spawner spawn : _spawns)
			npcs.addAll(spawn.getAllSpawned());
		return npcs.isEmpty() ? Collections.<NpcInstance> emptyList() : npcs;
	}

	public NpcInstance getFirstSpawned()
	{
		List<NpcInstance> npcs = getAllSpawned();
		return npcs.size() > 0 ? npcs.get(0) : null;
	}

	public boolean isSpawned()
	{
		return _spawned;
	}
}