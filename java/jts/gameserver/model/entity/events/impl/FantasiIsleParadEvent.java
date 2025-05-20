package jts.gameserver.model.entity.events.impl;

import jts.commons.collections.MultiValueSet;
import jts.gameserver.model.entity.events.GlobalEvent;

public class FantasiIsleParadEvent extends GlobalEvent
{
	public FantasiIsleParadEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	public void reCalcNextTime(boolean onStart)
	{
		clearActions();
	}

	protected long startTimeMillis()
	{
		return System.currentTimeMillis() + 30000L;
	}
}