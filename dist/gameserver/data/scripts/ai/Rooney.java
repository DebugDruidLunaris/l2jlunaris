package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.utils.Location;

public class Rooney extends DefaultAI
{
	static final Location[] points = 
	{
		new Location(184022, -117083, -3342),
		new Location(183516, -118815, -3093),
		new Location(185007, -115651, -1587),
		new Location(186191, -116465, -1587),
		new Location(189630, -115611, -1587) 
	};

	private static final long TELEPORT_PERIOD = 30 * 60 * 1000; // 30 min
	private long _lastTeleport = System.currentTimeMillis();

	public Rooney(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(System.currentTimeMillis() - _lastTeleport < TELEPORT_PERIOD)
			return false;

		for(int i = 0; i < points.length; i++)
		{
			Location loc = points[Rnd.get(points.length)];
			if(actor.getLoc().equals(loc))
				continue;

			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 1000, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(loc), 1000);
			_lastTeleport = System.currentTimeMillis();
			break;
		}
		return true;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}