package jts.gameserver.listener.actor.npc;

import jts.gameserver.listener.NpcListener;
import jts.gameserver.model.instances.NpcInstance;

public interface OnDecayListener extends NpcListener
{
	public void onDecay(NpcInstance actor);
}