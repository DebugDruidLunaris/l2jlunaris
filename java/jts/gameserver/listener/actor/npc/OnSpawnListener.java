package jts.gameserver.listener.actor.npc;

import jts.gameserver.listener.NpcListener;
import jts.gameserver.model.instances.NpcInstance;

public interface OnSpawnListener extends NpcListener
{
	public void onSpawn(NpcInstance actor);
}