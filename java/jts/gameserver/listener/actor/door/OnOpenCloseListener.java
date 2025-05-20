package jts.gameserver.listener.actor.door;

import jts.gameserver.listener.CharListener;
import jts.gameserver.model.instances.DoorInstance;

public interface OnOpenCloseListener extends CharListener
{
	void onOpen(DoorInstance doorInstance);
	void onClose(DoorInstance doorInstance);
}