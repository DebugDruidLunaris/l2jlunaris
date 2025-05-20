package jts.gameserver.listener.game;

import jts.gameserver.listener.GameListener;

public interface OnSSPeriodListener extends GameListener
{
	public void onPeriodChange(int val);
}