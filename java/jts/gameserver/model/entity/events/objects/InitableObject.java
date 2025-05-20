package jts.gameserver.model.entity.events.objects;

import java.io.Serializable;
import jts.gameserver.model.entity.events.GlobalEvent;

public interface InitableObject extends Serializable
{
	void initObject(GlobalEvent e);
}