package jts.gameserver.templates.mapregion;

import java.util.Map;

import jts.gameserver.model.Territory;
import jts.gameserver.model.base.Race;

public class RestartArea implements RegionData
{
	private final Territory _territory;
	private final Map<Race, RestartPoint> _restarts;

	public RestartArea(Territory territory, Map<Race, RestartPoint> restarts)
	{
		_territory = territory;
		_restarts = restarts;
	}

	@Override
	public Territory getTerritory()
	{
		return _territory;
	}

	public Map<Race, RestartPoint> getRestartPoint()
	{
		return _restarts;
	}
}