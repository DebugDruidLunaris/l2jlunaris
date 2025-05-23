package npc.model.events;

import jts.commons.util.Rnd;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;
import services.TeleToFantasyIsle;

@SuppressWarnings("serial")
public class ColiseumHelperInstance extends NpcInstance
{
	private Location[][] LOCS = new Location[][] 
	{
		{ new Location(-84451, -45452, -10728), new Location(-84580, -45587, -10728) },
		{ new Location(-86154, -50429, -10728), new Location(-86118, -50624, -10728) },
		{ new Location(-82009, -53652, -10728), new Location(-81802, -53665, -10728) },
		{ new Location(-77603, -50673, -10728), new Location(-77586, -50503, -10728) },
		{ new Location(-79186, -45644, -10728), new Location(-79309, -45561, -10728) } 
	};

	public ColiseumHelperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("teleOut"))
			player.teleToLocation(TeleToFantasyIsle.POINTS[Rnd.get(TeleToFantasyIsle.POINTS.length)]);
		else if(command.startsWith("coliseum"))
		{
			int a = Integer.parseInt(String.valueOf(command.charAt(9)));
			Location[] locs = LOCS[a];

			player.teleToLocation(locs[Rnd.get(locs.length)]);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		showChatWindow(player, "scripts/events/UndergroundColiseum/coliseum_helper_index.htm");
	}
}