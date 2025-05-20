package jts.gameserver.listener.actor.door.impl;

import jts.gameserver.listener.actor.door.OnOpenCloseListener;
import jts.gameserver.model.instances.DoorInstance;

public class MasterOnOpenCloseListenerImpl implements OnOpenCloseListener
{
	private DoorInstance _door;

	public MasterOnOpenCloseListenerImpl(DoorInstance door)
	{
		_door = door;
	}

	@Override
	public void onOpen(DoorInstance doorInstance)
	{
		_door.openMe();
	}

	@Override
	public void onClose(DoorInstance doorInstance)
	{
		_door.closeMe();
	}
}