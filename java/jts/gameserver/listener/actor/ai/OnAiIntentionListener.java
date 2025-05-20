package jts.gameserver.listener.actor.ai;

import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.listener.AiListener;
import jts.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener
{
	public void onAiIntention(Creature actor, CtrlIntention intention, Object arg0, Object arg1);
}