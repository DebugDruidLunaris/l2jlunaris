package events.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerExitListener;
import jts.gameserver.listener.actor.player.OnTeleportListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.ReflectionUtils;

public class DionArena extends Functions implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener
{
	private static class DionArenaImpl extends ArenaTemplate
	{
		@Override
		protected void onLoad()
		{
			_managerId = 20230001;
			_className = "DionArena";
			_status = 0;

			_team1list = new CopyOnWriteArrayList<Long>();
			_team2list = new CopyOnWriteArrayList<Long>();
			_team1live = new CopyOnWriteArrayList<Long>();
			_team2live = new CopyOnWriteArrayList<Long>();

			_expToReturn = new HashMap<Integer, Integer>();
			_classToReturn = new HashMap<Integer, Integer>();

			_zoneListener = new ZoneListener();
			_zone = ReflectionUtils.getZone("[dion_monster_pvp]");
			_zone.addListener(_zoneListener);

			_team1points = new ArrayList<Location>();
			_team2points = new ArrayList<Location>();

			_team1points.add(new Location(12053, 183101, -3563));
			_team1points.add(new Location(12253, 183101, -3563));
			_team1points.add(new Location(12459, 183101, -3563));
			_team1points.add(new Location(12659, 183101, -3563));
			_team1points.add(new Location(12851, 183101, -3563));
			_team2points.add(new Location(12851, 183941, -3563));
			_team2points.add(new Location(12659, 183941, -3563));
			_team2points.add(new Location(12459, 183941, -3563));
			_team2points.add(new Location(12253, 183941, -3563));
			_team2points.add(new Location(12053, 183941, -3563));
		}

		@Override
		protected void onReload()
		{
			if(_status > 0)
				template_stop();
			_zone.removeListener(_zoneListener);
		}
	}

	private static ArenaTemplate _instance = new DionArenaImpl();

	public static ArenaTemplate getInstance()
	{
		if(_instance == null)
			_instance = new DionArenaImpl();
		return _instance;
	}

	@Override
	public void onLoad()
	{
		getInstance().onLoad();
		CharListenerList.addGlobal(this);
	}

	@Override
	public void onReload()
	{
		getInstance().onReload();
		_instance = null;
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		getInstance().onDeath(cha, killer);
	}

	@Override
	public void onPlayerExit(Player player)
	{
		getInstance().onPlayerExit(player);
	}

	@Override
	public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
	{
		getInstance().onTeleport(player);
	}

	public String DialogAppend_20230001(Integer val)
	{
		if(val == 0)
		{
			Player player = getSelf();
			if(player.isGM())
				return HtmCache.getInstance().getNotNull("scripts/events/arena/20230001.htm", player) + HtmCache.getInstance().getNotNull("scripts/events/arena/20230001-4.htm", player);
			return HtmCache.getInstance().getNotNull("scripts/events/arena/20230001.htm", player);
		}
		return "";
	}

	public String DialogAppend_20230002(Integer val)
	{
		return DialogAppend_20230001(val);
	}

	public void create1()
	{
		getInstance().template_create1(getSelf());
	}

	public void create2()
	{
		getInstance().template_create2(getSelf());
	}

	public void register()
	{
		getInstance().template_register(getSelf());
	}

	public void check1(String[] var)
	{
		getInstance().template_check1(getSelf(), var);
	}

	public void check2(String[] var)
	{
		getInstance().template_check2(getSelf(), var);
	}

	public void register_check(String[] var)
	{
		getInstance().template_register_check(getSelf(), var);
	}

	public void stop()
	{
		getInstance().template_stop();
	}

	public void announce()
	{
		getInstance().template_announce();
	}

	public void prepare()
	{
		getInstance().template_prepare();
	}

	public void start()
	{
		getInstance().template_start();
	}

	public static void timeOut()
	{
		getInstance().template_timeOut();
	}
}