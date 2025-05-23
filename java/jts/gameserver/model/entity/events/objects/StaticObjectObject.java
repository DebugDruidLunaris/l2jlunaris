package jts.gameserver.model.entity.events.objects;

import jts.gameserver.data.xml.holder.StaticObjectHolder;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.instances.StaticObjectInstance;

@SuppressWarnings("serial")
public class StaticObjectObject implements SpawnableObject
{
	private int _uid;
	private StaticObjectInstance _instance;

	public StaticObjectObject(int id)
	{
		_uid = id;
	}

	@Override
	public void spawnObject(GlobalEvent event)
	{
		_instance = StaticObjectHolder.getInstance().getObject(_uid);
	}

	@Override
	public void despawnObject(GlobalEvent event) {}

	@Override
	public void refreshObject(GlobalEvent event)
	{
		if(!event.isInProgress())
			_instance.removeEvent(event);
		else
			_instance.addEvent(event);
	}

	public void setMeshIndex(int id)
	{
		_instance.setMeshIndex(id);
		_instance.broadcastInfo(false);
	}

	public int getUId()
	{
		return _uid;
	}
}