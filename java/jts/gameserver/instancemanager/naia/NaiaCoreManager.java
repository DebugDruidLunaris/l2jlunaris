package jts.gameserver.instancemanager.naia;

import jts.commons.geometry.Polygon;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Territory;
import jts.gameserver.model.Zone;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NaiaCoreManager
{
	private static final Logger _log = LoggerFactory.getLogger(NaiaTowerManager.class);
	private static final NaiaCoreManager _instance = new NaiaCoreManager();
	private static Zone _zone;
	private static boolean _active = false;
	private static boolean _bossSpawned = false;
	private static final Territory _coreTerritory = new Territory().add(new Polygon().add(-44789, 246305).add(-44130, 247452).add(-46092, 248606).add(-46790, 247414).add(-46139, 246304).setZmin(-14220).setZmax(-13800));

	//Spores
	private static final int fireSpore = 25605;
	private static final int waterSpore = 25606;
	private static final int windSpore = 25607;
	private static final int earthSpore = 25608;
	//Bosses
	private static final int fireEpidos = 25609;
	private static final int waterEpidos = 25610;
	private static final int windEpidos = 25611;
	private static final int earthEpidos = 25612;

	private static final int teleCube = 32376;

	private static final int respawnDelay = 120; // 2min
	private static final long coreClearTime = 4 * 60 * 60 * 1000L; // 4hours
	private static final Location spawnLoc = new Location(-45496, 246744, -14209);

	public static final NaiaCoreManager getInstance()
	{
		return _instance;
	}

	public NaiaCoreManager()
	{
		_zone = ReflectionUtils.getZone("[naia_core_poison]");
		_log.info("Naia Core Manager: Carregado");
	}

	public static void launchNaiaCore()
	{
		if(isActive())
			return;

		_active = true;
		ReflectionUtils.getDoor(18250025).closeMe();
		_zone.setActive(true);
		spawnSpores();
		ThreadPoolManager.getInstance().schedule(new ClearCore(), coreClearTime);
	}

	private static boolean isActive()
	{
		return _active;
	}

	public static void setZoneActive(boolean value)
	{
		_zone.setActive(value);
	}

	private static void spawnSpores()
	{
		spawnToRoom(fireSpore, 10, _coreTerritory);
		spawnToRoom(waterSpore, 10, _coreTerritory);
		spawnToRoom(windSpore, 10, _coreTerritory);
		spawnToRoom(earthSpore, 10, _coreTerritory);
	}

	public static void spawnEpidos(int index)
	{
		if(!isActive())
			return;
		int epidostospawn = 0;
		switch(index)
		{
			case 1:
			{
				epidostospawn = fireEpidos;
				break;
			}
			case 2:
			{
				epidostospawn = waterEpidos;
				break;
			}
			case 3:
			{
				epidostospawn = windEpidos;
				break;
			}
			case 4:
			{
				epidostospawn = earthEpidos;
				break;
			}
			default:
				break;
		}
		try
		{
			SimpleSpawner sp = new SimpleSpawner(epidostospawn);
			sp.setLoc(spawnLoc);
			sp.doSpawn(true);
			sp.stopRespawn();
			_bossSpawned = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean isBossSpawned()
	{
		return _bossSpawned;
	}

	public static void removeSporesAndSpawnCube()
	{
		int[] spores = { fireSpore, waterSpore, windSpore, earthSpore };
		for(NpcInstance spore : GameObjectsStorage.getAllByNpcId(spores, false))
			spore.deleteMe();
		try
		{
			SimpleSpawner sp = new SimpleSpawner(teleCube);
			sp.setLoc(spawnLoc);
			sp.doSpawn(true);
			sp.stopRespawn();
			Functions.npcShout(sp.getLastSpawn(), "Teleportation to Beleth Throne Room is available for 2 minutes");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static class ClearCore extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			int[] spores = { fireSpore, waterSpore, windSpore, earthSpore };
			int[] epidoses = { fireEpidos, waterEpidos, windEpidos, earthEpidos };
			for(NpcInstance spore : GameObjectsStorage.getAllByNpcId(spores, false))
				spore.deleteMe();
			for(NpcInstance epidos : GameObjectsStorage.getAllByNpcId(epidoses, false))
				epidos.deleteMe();

			_active = false;
			ReflectionUtils.getDoor(18250025).openMe();
			_zone.setActive(false);
		}
	}

	private static void spawnToRoom(int mobId, int count, Territory territory)
	{
		for(int i = 0; i < count; i++)
			try
			{
				SimpleSpawner sp = new SimpleSpawner(mobId);
				sp.setLoc(Territory.getRandomLoc(territory).setH(Rnd.get(65535)));
				sp.setRespawnDelay(respawnDelay, 30);
				sp.setAmount(1);
				sp.doSpawn(true);
				sp.startRespawn();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
}