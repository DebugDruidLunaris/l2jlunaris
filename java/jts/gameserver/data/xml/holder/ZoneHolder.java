package jts.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.templates.ZoneTemplate;

public class ZoneHolder extends AbstractHolder
{
	private static final ZoneHolder _instance = new ZoneHolder();

	private final Map<String, ZoneTemplate> _zones = new HashMap<String, ZoneTemplate>();

	public static ZoneHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(ZoneTemplate zone)
	{
		_zones.put(zone.getName(), zone);
	}

	public ZoneTemplate getTemplate(String name)
	{
		return _zones.get(name);
	}

	public Map<String, ZoneTemplate> getZones()
	{
		return _zones;
	}

	@Override
	public int size()
	{
		return _zones.size();
	}

	@Override
	public void clear()
	{
		_zones.clear();
	}
}