package jts.gameserver.templates.spawn;

import jts.gameserver.utils.Location;

public interface SpawnRange
{
	Location getRandomLoc(int geoIndex);
}