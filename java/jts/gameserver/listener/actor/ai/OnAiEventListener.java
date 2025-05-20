package jts.gameserver.listener.actor.ai;

import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.listener.AiListener;
import jts.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener
{
	public void onAiEvent(Creature actor, CtrlEvent evt, Object[] args);
}