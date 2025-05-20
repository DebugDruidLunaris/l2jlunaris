package jts.gameserver.listener.game;

import jts.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener
{
	public void onDay();
	public void onNight();
}