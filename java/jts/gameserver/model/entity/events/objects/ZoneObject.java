package jts.gameserver.model.entity.events.objects;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.events.GlobalEvent;

@SuppressWarnings("serial")
public class ZoneObject implements InitableObject
{
	private String _name;
	private Zone _zone;

	public ZoneObject(String name)
	{
		_name = name;
	}

	@Override
	public void initObject(GlobalEvent e)
	{
		Reflection r = e.getReflection();

		_zone = r.getZone(_name);
	}

	public void setActive(boolean a)
	{
		_zone.setActive(a);
	}

	public void setActive(boolean a, GlobalEvent event)
	{
		setActive(a);
	}

	public Zone getZone()
	{
		return _zone;
	}

	public List<Player> getInsidePlayers()
	{
		return _zone.getInsidePlayers();
	}

	public boolean checkIfInZone(Creature c)
	{
		return _zone.checkIfInZone(c);
	}
}