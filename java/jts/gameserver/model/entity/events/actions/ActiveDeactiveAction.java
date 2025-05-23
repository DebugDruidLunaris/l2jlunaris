package jts.gameserver.model.entity.events.actions;

import jts.gameserver.model.entity.events.EventAction;
import jts.gameserver.model.entity.events.GlobalEvent;

public class ActiveDeactiveAction implements EventAction
{
	private final boolean _active;
	private final String _name;

	public ActiveDeactiveAction(boolean active, String name)
	{
		_active = active;
		_name = name;
	}

	@Override
	public void call(GlobalEvent event)
	{
		event.zoneAction(_name, _active);
	}
}