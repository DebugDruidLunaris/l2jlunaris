package jts.gameserver.listener.actor.player;

import jts.gameserver.listener.PlayerListener;

public interface OnAnswerListener extends PlayerListener
{
	void sayYes();
	void sayNo();
}