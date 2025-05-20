package jts.gameserver.model.entity.events.objects;

import java.io.Serializable;
import jts.gameserver.model.entity.events.GlobalEvent;

public interface SpawnableObject extends Serializable
{
	void spawnObject(GlobalEvent event);
	void despawnObject(GlobalEvent event);
	void refreshObject(GlobalEvent event);
}