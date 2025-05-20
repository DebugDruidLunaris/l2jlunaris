package jts.gameserver.listener.event;

import jts.gameserver.listener.EventListener;
import jts.gameserver.model.entity.events.GlobalEvent;

public interface OnStartStopListener extends EventListener
{
	void onStart(GlobalEvent event);
	void onStop(GlobalEvent event);
}