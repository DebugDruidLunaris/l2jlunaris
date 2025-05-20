package jts.gameserver.model.entity.events.impl;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.GlobalEvent;

public class UndergroundColiseumBattleEvent extends GlobalEvent
{
	protected UndergroundColiseumBattleEvent(Player player1, Player player2)
	{
		super(0, player1.getObjectId() + "_" + player2.getObjectId());
	}

	@Override
	public void announce(int val)
	{
		switch(val)
		{
			case -180:
			case -120:
			case -60:
				break;
		}
	}

	@Override
	public void reCalcNextTime(boolean onInit)
	{
		registerActions();
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 180000L;
	}
}