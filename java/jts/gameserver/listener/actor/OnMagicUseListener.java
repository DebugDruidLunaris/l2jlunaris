package jts.gameserver.listener.actor;

import jts.gameserver.listener.CharListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;

public interface OnMagicUseListener extends CharListener
{
	public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt);
}