package jts.gameserver.listener.actor;

import jts.gameserver.listener.CharListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;

public interface OnMagicHitListener extends CharListener
{
	public void onMagicHit(Creature actor, Skill skill, Creature caster);
}