package jts.gameserver.listener.actor;

import jts.gameserver.listener.CharListener;
import jts.gameserver.model.Creature;

public interface OnDeathListener extends CharListener
{
	public void onDeath(Creature actor, Creature killer);
}