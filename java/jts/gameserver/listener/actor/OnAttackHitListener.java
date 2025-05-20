package jts.gameserver.listener.actor;

import jts.gameserver.listener.CharListener;
import jts.gameserver.model.Creature;

public interface OnAttackHitListener extends CharListener
{
	public void onAttackHit(Creature actor, Creature attacker);
}