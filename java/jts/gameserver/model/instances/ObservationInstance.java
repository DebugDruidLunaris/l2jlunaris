package jts.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.Zone;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public final class ObservationInstance extends NpcInstance
{
	public ObservationInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(checkForDominionWard(player))
			return;

		if(player.getOlympiadGame() != null)
			return;

		if(command.startsWith("observeSiege"))
		{
			String val = command.substring(13);
			StringTokenizer st = new StringTokenizer(val);
			st.nextToken(); // Bypass cost

			List<Zone> zones = new ArrayList<Zone>();
			World.getZones(zones, new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())), ReflectionManager.DEFAULT);
			for(Zone z : zones)
				if(z.getType() == Zone.ZoneType.SIEGE && z.isActive())
				{
					doObserve(player, val);
					return;
				}

			player.sendPacket(SystemMsg.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE);
		}
		else if(command.startsWith("observe"))
			doObserve(player, command.substring(8));
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "observation/" + pom + ".htm";
	}

	private void doObserve(Player player, String val)
	{
		StringTokenizer st = new StringTokenizer(val);
		int cost = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());

		if(!player.reduceAdena(cost, true))
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			player.sendActionFailed();
			return;
		}

		player.enterObserverMode(new Location(x, y, z));
	}
}