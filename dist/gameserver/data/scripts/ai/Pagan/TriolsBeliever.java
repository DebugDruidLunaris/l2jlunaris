package ai.Pagan;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class TriolsBeliever extends Mystic
{
	private boolean _tele = true;
	public static final Location[] locs = {new Location( -16128, -35888, -10726), new Location( -16397, -44970, -10724), new Location( -15729, -42001, -10724)};

	public TriolsBeliever(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		for(Player player : World.getAroundPlayers(actor, 500, 500))
		{
			if(player == null || !player.isInParty())
				continue;

			if(player.getParty().getMemberCount() >= 5 && _tele)
			{
				_tele = false;
				player.teleToLocation(Rnd.get(locs));
			}
		}

		return true;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_tele = true;
		super.onEvtDead(killer);
	}
}