package jts.gameserver.data;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.lang.reflect.Constructor;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.entity.boat.Boat;
import jts.gameserver.templates.CharTemplate;

public final class BoatHolder extends AbstractHolder
{
	public static final CharTemplate TEMPLATE = new CharTemplate(CharTemplate.getEmptyStatsSet());

	private static BoatHolder _instance = new BoatHolder();
	private final TIntObjectHashMap<Boat> _boats = new TIntObjectHashMap<Boat>();

	public static BoatHolder getInstance()
	{
		return _instance;
	}

	public void spawnAll()
	{
		log();
		for(TIntObjectIterator<Boat> iterator = _boats.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			iterator.value().spawnMe();
			info("Spawning: " + iterator.value().getName());
		}
	}

	@SuppressWarnings("rawtypes")
	public Boat initBoat(String name, String clazz)
	{
		try
		{
			Class<?> cl = Class.forName("jts.gameserver.model.entity.boat." + clazz);
			Constructor constructor = cl.getConstructor(Integer.TYPE, CharTemplate.class);

			Boat boat = (Boat) constructor.newInstance(IdFactory.getInstance().getNextId(), TEMPLATE);
			boat.setName(name);
			addBoat(boat);
			return boat;
		}
		catch(Exception e)
		{
			error("Fail to init boat: " + clazz, e);
		}

		return null;
	}

	public Boat getBoat(String name)
	{
		for(TIntObjectIterator<Boat> iterator = _boats.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			if(iterator.value().getName().equals(name))
				return iterator.value();
		}

		return null;
	}

	public Boat getBoat(int objectId)
	{
		return _boats.get(objectId);
	}

	public void addBoat(Boat boat)
	{
		_boats.put(boat.getObjectId(), boat);
	}

	public void removeBoat(Boat boat)
	{
		_boats.remove(boat.getObjectId());
	}

	@Override
	public int size()
	{
		return _boats.size();
	}

	@Override
	public void clear()
	{
		_boats.clear();
	}
}