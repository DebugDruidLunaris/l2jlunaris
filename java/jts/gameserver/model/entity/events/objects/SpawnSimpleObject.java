package jts.gameserver.model.entity.events.objects;

import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.NpcUtils;

@SuppressWarnings("serial")
public class SpawnSimpleObject implements SpawnableObject
{
	private int _npcId;
	private Location _loc;

	private NpcInstance _npc;

	public SpawnSimpleObject(int npcId, Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}

	@Override
	public void spawnObject(GlobalEvent event)
	{
		_npc = NpcUtils.spawnSingle(_npcId, _loc, event.getReflection());
		_npc.addEvent(event);
	}

	@Override
	public void despawnObject(GlobalEvent event)
	{
		_npc.removeEvent(event);
		_npc.deleteMe();
	}

	@Override
	public void refreshObject(GlobalEvent event) {}
}