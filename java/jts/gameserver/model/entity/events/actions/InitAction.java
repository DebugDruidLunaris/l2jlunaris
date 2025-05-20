package jts.gameserver.model.entity.events.actions;

import jts.gameserver.model.entity.events.EventAction;
import jts.gameserver.model.entity.events.GlobalEvent;

public class InitAction implements EventAction
{
	private String _name;

	public InitAction(String name)
	{
		_name = name;
	}

	@Override
	public void call(GlobalEvent event)
	{
		event.initAction(_name);
	}
}