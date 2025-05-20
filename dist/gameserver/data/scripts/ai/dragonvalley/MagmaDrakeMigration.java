package ai.dragonvalley;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class MagmaDrakeMigration extends Fighter
{
	private Location[] _points;
	
	private Location[] migration1_route = new Location[]{new Location(125128, 111416, -3152),
			new Location(125496, 112152, -3232),
			new Location(125800, 112520, -3320),
			new Location(126264, 112776, -3448),
			new Location(126712, 112920, -3552),
			new Location(127464, 113032, -3664),
			new Location(128312, 113400, -3680),
			new Location(129128, 113992, -3712),
			new Location(129352, 114760, -3792),
			new Location(128984, 115352, -3808),
			new Location(128264, 115624, -3792),
			new Location(127368, 115512, -3744),
			new Location(125960, 115128, -3728),
			new Location(124936, 115112, -3728),
			new Location(124216, 115192, -3680)};
	
	private Location[] migration2_route = new Location[]{new Location(118120, 116664, -3728),
			new Location(117320, 116792, -3728),
			new Location(116632, 117208, -3728),
			new Location(116408, 117928, -3728),
			new Location(116392, 118536, -3728),
			new Location(116136, 119304, -3728),
			new Location(115720, 119816, -3680),
			new Location(115128, 120152, -3680),
			new Location(114104, 120680, -3808),
			new Location(113496, 121608, -3728),
			new Location(112728, 122136, -3728),
			new Location(111960, 122552, -3728)};
	
	private Location[] migration3_route = new Location[]{new Location(109640, 112328, -3040),
			new Location(110648, 111096, -3120),
			new Location(111608, 110472, -3056),
			new Location(112632, 110264, -2976),
			new Location(114040, 110184, -3024),
			new Location(115128, 110024, -3040),
			new Location(116232, 110008, -3040),
			new Location(117352, 109960, -2960),
			new Location(119000, 109768, -2976),
			new Location(120360, 109336, -2992),
			new Location(121944, 108648, -2976)};
	
	private Location[] migration4_route = new Location[]{new Location(109256, 122808, -3664),
			new Location(108472, 122408, -3664),
			new Location(107576, 122344, -3696),
			new Location(106728, 122952, -3696),
			new Location(105800, 123432, -3648),
			new Location(104760, 123816, -3728),
			new Location(103752, 123688, -3728),
			new Location(103160, 123384, -3696),
			new Location(102664, 122680, -3648),
			new Location(102888, 121848, -3696),
			new Location(102984, 121016, -3728)};

	private int _lastPoint = 0;
	private boolean _firstThought = true;
	private static final int spawnDistance = 2500;

	public MagmaDrakeMigration(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = Integer.MAX_VALUE - 10;
	}

	public boolean isGlobalAI()
	{
		return true;
	}
	
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		
		Location migration1_spawnLoc = new Location(124760,	109608,	-3088);
		Location migration2_spawnLoc = new Location(119224,	116264,	-3760);
		Location migration3_spawnLoc = new Location(109464,	113688,	-3072);
		Location migration4_spawnLoc = new Location(110024,	123480,	-3616);
		
		if(actor.getDistance(migration1_spawnLoc) < spawnDistance)
			_points = migration1_route;
		if(actor.getDistance(migration2_spawnLoc) < spawnDistance)
			_points = migration2_route;
		if(actor.getDistance(migration3_spawnLoc) < spawnDistance)
			_points = migration3_route;
		if(actor.getDistance(migration4_spawnLoc) < spawnDistance)
			_points = migration4_route;
		
		super.onEvtSpawn();
	}

	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		if(!getActor().isMoving)
			startMoveTask();

		return true;
	}

	protected void onEvtArrived()
	{
		startMoveTask();
		super.onEvtArrived();
	}

	private void startMoveTask()
	{
		NpcInstance npc = getActor();
		if(_firstThought)
		{
			_lastPoint = getIndex(Location.findNearest(npc, _points));
			_firstThought = false;
		}
		else
			_lastPoint++;
		if(_lastPoint >= _points.length)
		{
			_lastPoint = 0;
			npc.deleteMe();
		}
		npc.setRunning();
		try
		{
			addTaskMove(Location.findPointToStay(_points[_lastPoint], 150, npc.getGeoIndex()), true);
		}
		catch(Exception e)
		{}
		doTask();
	}

	private int getIndex(Location loc)
	{
		for(int i = 0; i < _points.length; i++)
			if(_points[i] == loc)
				return i;
		return 0;
	}

	protected boolean randomWalk()
	{
		return false;
	}

	protected boolean maybeMoveToHome()
	{
		return false;
	}

	protected void teleportHome()
	{}

	protected void returnHome(boolean clearAggro, boolean teleport)
	{
		super.returnHome(clearAggro, teleport);
		clearTasks();
		_firstThought = true;
		startMoveTask();
	}
}
