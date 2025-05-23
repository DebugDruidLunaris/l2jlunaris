package jts.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.templates.item.support.FishGroup;
import jts.gameserver.templates.item.support.FishTemplate;
import jts.gameserver.templates.item.support.LureTemplate;
import jts.gameserver.templates.item.support.LureType;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class FishDataHolder extends AbstractHolder
{
	private static final FishDataHolder _instance = new FishDataHolder();

	private List<FishTemplate> _fishes = new ArrayList<FishTemplate>();
	private IntObjectMap<LureTemplate> _lures = new HashIntObjectMap<LureTemplate>();
	private IntObjectMap<Map<LureType, Map<FishGroup, Integer>>> _distributionsForZones = new HashIntObjectMap<Map<LureType, Map<FishGroup, Integer>>>();

	public static FishDataHolder getInstance()
	{
		return _instance;
	}

	public void addFish(FishTemplate fishTemplate)
	{
		_fishes.add(fishTemplate);
	}

	public void addLure(LureTemplate template)
	{
		_lures.put(template.getItemId(), template);
	}

	public void addDistribution(int id, LureType lureType, Map<FishGroup, Integer> map)
	{
		Map<LureType, Map<FishGroup, Integer>> byLureType = _distributionsForZones.get(id);
		if(byLureType == null)
			_distributionsForZones.put(id, byLureType = new HashMap<LureType, Map<FishGroup, Integer>>());

		byLureType.put(lureType, map);
	}

	@Override
	public void log()
	{
		info("Carregado " + _fishes.size() + " fish(es).");
		info("Carregado " + _lures.size() + " lure(s).");
		info("Carregado " + _distributionsForZones.size() + " distribution(s).");
	}

	@Deprecated
	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		_fishes.clear();
		_lures.clear();
		_distributionsForZones.clear();
	}
}