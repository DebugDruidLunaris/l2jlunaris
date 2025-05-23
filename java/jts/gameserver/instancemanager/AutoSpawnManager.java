package jts.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jts.commons.dbutils.DbUtils;
import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.base.Race;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.mapregion.RestartArea;
import jts.gameserver.templates.mapregion.RestartPoint;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoSpawnManager
{
	private static final Logger _log = LoggerFactory.getLogger(AutoSpawnManager.class);
	private static AutoSpawnManager _instance;

	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; //1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; //1 hour in millisecs

	protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> _runningSpawns;

	public AutoSpawnManager()
	{
		_registeredSpawns = new ConcurrentHashMap<Integer, AutoSpawnInstance>();
		_runningSpawns = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();

		restoreSpawnData();

		_log.info("AutoSpawnHandler: Loaded " + size() + " handlers in total.");
	}

	public static AutoSpawnManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new AutoSpawnManager();
		}

		return _instance;
	}

	public final int size()
	{
		return _registeredSpawns.size();
	}

	private void restoreSpawnData()
	{
		@SuppressWarnings("unused")
		int numLoaded = 0;
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null, rset2 = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			// Restore spawn group data, then the location data.
			statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
			statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");

			rset = statement.executeQuery();
			while(rset.next())
			{
				// Register random spawn group, set various options on the created spawn instance.
				AutoSpawnInstance spawnInst = registerSpawn(rset.getInt("npcId"), rset.getInt("initialDelay"), rset.getInt("respawnDelay"), rset.getInt("despawnDelay"));
				spawnInst.setSpawnCount(rset.getInt("count"));
				spawnInst.setBroadcast(rset.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rset.getBoolean("randomSpawn"));
				numLoaded++;

				// Restore the spawn locations for this spawn group/instance.

				statement2.setInt(1, rset.getInt("groupId"));
				rset2 = statement2.executeQuery();
				while(rset2.next())
				{
					// Add each location to the spawn group/instance.
					spawnInst.addSpawnLocation(rset2.getInt("x"), rset2.getInt("y"), rset2.getInt("z"), rset2.getInt("heading"));
				}
				DbUtils.close(rset2);
			}
		}
		catch(Exception e)
		{
			_log.warn("AutoSpawnHandler: Could not restore spawn data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if(initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}

		if(respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}

		if(despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}

		AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);

		if(spawnPoints != null)
		{
			for(int[] spawnPoint : spawnPoints)
			{
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}

		int newId = IdFactory.getInstance().getNextId();
		newSpawn._objectId = newId;
		_registeredSpawns.put(newId, newSpawn);

		setSpawnActive(newSpawn, true);

		return newSpawn;
	}

	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}

	public boolean removeSpawn(AutoSpawnInstance spawnInst)
	{
		if(!isSpawnRegistered(spawnInst))
			return false;

		try
		{
			// Try to remove from the list of registered spawns if it exists.
			_registeredSpawns.remove(spawnInst.getNpcId());

			// Cancel the currently associated running scheduled task.
			ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst._objectId);
			respawnTask.cancel(false);
		}
		catch(Exception e)
		{
			_log.warn("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e);
			return false;
		}

		return true;
	}

	public void removeSpawn(int objectId)
	{
		removeSpawn(_registeredSpawns.get(objectId));
	}

	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
	{
		int objectId = spawnInst._objectId;

		if(isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;

			if(isActive)
			{
				AutoSpawner rset = new AutoSpawner(objectId);
				if(spawnInst._desDelay > 0)
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(rset, spawnInst._initDelay, spawnInst._resDelay);
				}
				else
				{
					spawnTask = ThreadPoolManager.getInstance().schedule(rset, spawnInst._initDelay);
				}
				//spawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(rset, spawnInst._initDelay, spawnInst._resDelay);
				_runningSpawns.put(objectId, spawnTask);
			}
			else
			{
				spawnTask = _runningSpawns.remove(objectId);

				if(spawnTask != null)
				{
					spawnTask.cancel(false);
				}
			}

			spawnInst.setSpawnActive(isActive);
		}
	}

	public final long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
	{
		int objectId = spawnInst._objectId;

		if(!isSpawnRegistered(objectId))
			return -1;

		return _runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}

	public final AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if(isObjectId)
		{
			if(isSpawnRegistered(id))
				return _registeredSpawns.get(id);
		}
		else
		{
			for(AutoSpawnInstance spawnInst : _registeredSpawns.values())
				if(spawnInst._npcId == id)
					return spawnInst;
		}

		return null;
	}

	public Map<Integer, AutoSpawnInstance> getAllAutoSpawnInstance(int id)
	{
		Map<Integer, AutoSpawnInstance> spawnInstList = new ConcurrentHashMap<Integer, AutoSpawnInstance>();

		for(AutoSpawnInstance spawnInst : _registeredSpawns.values())
			if(spawnInst._npcId == id)
			{
				spawnInstList.put(spawnInst._objectId, spawnInst);
			}

		return spawnInstList;
	}

	public final boolean isSpawnRegistered(int objectId)
	{
		return _registeredSpawns.containsKey(objectId);
	}

	public final boolean isSpawnRegistered(AutoSpawnInstance spawnInst)
	{
		return _registeredSpawns.containsValue(spawnInst);
	}

	private class AutoSpawner extends RunnableImpl
	{
		private int _objectId;

		AutoSpawner(int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void runImpl() throws Exception
		{
			try
			{
				// Retrieve the required spawn instance for this spawn task.
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);

				// If the spawn is not scheduled to be active, cancel the spawn task.
				if(!spawnInst.isSpawnActive() || Config.DONTLOADSPAWN)
					return;

				Location[] locationList = spawnInst.getLocationList();

				// If there are no set co-ordinates, cancel the spawn task.
				if(locationList.length == 0)
				{
					_log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + _objectId + ").");
					return;
				}

				int locationCount = locationList.length;
				int locationIndex = Rnd.get(locationCount);

				if(!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst._lastLocIndex;
					locationIndex++;

					if(locationIndex == locationCount)
					{
						locationIndex = 0;
					}

					spawnInst._lastLocIndex = locationIndex;
				}

				// Set the X, Y and Z co-ordinates, where this spawn will take place.
				final int x = locationList[locationIndex].x;
				final int y = locationList[locationIndex].y;
				final int z = locationList[locationIndex].z;
				final int heading = locationList[locationIndex].h;

				// Fetch the template for this NPC ID and create a new spawn.
				NpcTemplate npcTemp = NpcHolder.getInstance().getTemplate(spawnInst.getNpcId());
				SimpleSpawner newSpawn = new SimpleSpawner(npcTemp);

				newSpawn.setLocx(x);
				newSpawn.setLocy(y);
				newSpawn.setLocz(z);
				if(heading != -1)
				{
					newSpawn.setHeading(heading);
				}
				newSpawn.setAmount(spawnInst.getSpawnCount());
				if(spawnInst._desDelay == 0)
				{
					newSpawn.setRespawnDelay(spawnInst._resDelay);
				}

				// Add the new spawn information to the spawn table, but do not store it.
				NpcInstance npcInst = null;

				for(int i = 0; i < spawnInst._spawnCount; i++)
				{
					npcInst = newSpawn.doSpawn(true);

					// To prevent spawning of more than one NPC in the exact same spot,
					// move it slightly by a small random offset.
					npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());

					// Add the NPC instance to the list of managed instances.
					spawnInst.addAttackable(npcInst);
				}

				if(npcInst == null)
					return;

				RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, npcInst.getLoc());
				String nearestTown = "";
				if(ra != null)
				{
					RestartPoint rp = ra.getRestartPoint().get(Race.human);
					nearestTown = rp.getNameLoc();
				}
				
				// Announce to all players that the spawn has taken place, with the nearest town location.
				if(spawnInst.isBroadcasting() && npcInst != null)
					DifferentMethods.sayToAll("jts.gameserver.model.AutoSpawnHandler.spawnMammon", new String[] {
							npcInst.getName(),
							nearestTown });

				// If there is no despawn time, do not create a despawn task.
				if(spawnInst.getDespawnDelay() > 0)
				{
					AutoDespawner rd = new AutoDespawner(_objectId);
					ThreadPoolManager.getInstance().schedule(rd, spawnInst.getDespawnDelay() - 1000);
				}
			}
			
			catch(Exception e)
			{
				_log.warn("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e);
				_log.error("", e);
			}
		}
	}

	private class AutoDespawner extends RunnableImpl
	{
		private int _objectId;

		AutoDespawner(int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void runImpl() throws Exception
		{
			try
			{
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);

				for(NpcInstance npcInst : spawnInst.getAttackableList())
				{
					npcInst.deleteMe();
					spawnInst.removeAttackable(npcInst);
				}
			}
			catch(Exception e)
			{
				_log.warn("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e);
			}
		}
	}

	public class AutoSpawnInstance
	{
		protected int _objectId;
		protected int _spawnIndex;

		protected int _npcId;
		protected int _initDelay;
		protected int _resDelay;
		protected int _desDelay;
		protected int _spawnCount = 1;
		protected int _lastLocIndex = -1;

		private List<NpcInstance> _npcList = new ArrayList<NpcInstance>();
		private List<Location> _locList = new ArrayList<Location>();

		private boolean _spawnActive;
		private boolean _randomSpawn = false;
		private boolean _broadcastAnnouncement = false;

		protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay)
		{
			_npcId = npcId;
			_initDelay = initDelay;
			_resDelay = respawnDelay;
			_desDelay = despawnDelay;
		}

		void setSpawnActive(boolean activeValue)
		{
			_spawnActive = activeValue;
		}

		boolean addAttackable(NpcInstance npcInst)
		{
			return _npcList.add(npcInst);
		}

		boolean removeAttackable(NpcInstance npcInst)
		{
			return _npcList.remove(npcInst);
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public int getInitialDelay()
		{
			return _initDelay;
		}

		public int getRespawnDelay()
		{
			return _resDelay;
		}

		public int getDespawnDelay()
		{
			return _desDelay;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getSpawnCount()
		{
			return _spawnCount;
		}

		public Location[] getLocationList()
		{
			return _locList.toArray(new Location[_locList.size()]);
		}

		public NpcInstance[] getAttackableList()
		{
			return _npcList.toArray(new NpcInstance[_npcList.size()]);
		}

		public Spawner[] getSpawns()
		{
			List<Spawner> npcSpawns = new ArrayList<Spawner>();

			for(NpcInstance npcInst : _npcList)
			{
				npcSpawns.add(npcInst.getSpawn());
			}

			return npcSpawns.toArray(new Spawner[npcSpawns.size()]);
		}

		public void setSpawnCount(int spawnCount)
		{
			_spawnCount = spawnCount;
		}

		public void setRandomSpawn(boolean randValue)
		{
			_randomSpawn = randValue;
		}

		public void setBroadcast(boolean broadcastValue)
		{
			_broadcastAnnouncement = broadcastValue;
		}

		public boolean isSpawnActive()
		{
			return _spawnActive;
		}

		public boolean isRandomSpawn()
		{
			return _randomSpawn;
		}

		public boolean isBroadcasting()
		{
			return _broadcastAnnouncement;
		}

		public boolean addSpawnLocation(int x, int y, int z, int heading)
		{
			return _locList.add(new Location(x, y, z, heading));
		}

		public boolean addSpawnLocation(int[] spawnLoc)
		{
			if(spawnLoc.length != 3)
				return false;

			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}

		public Location removeSpawnLocation(int locIndex)
		{
			try
			{
				return _locList.remove(locIndex);
			}
			catch(IndexOutOfBoundsException e)
			{
				return null;
			}
		}
	}
}