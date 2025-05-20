package npc.model.events;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.KrateisCubeEvent;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class KrateisCubeMatchManagerInstance extends NpcInstance
{
	//private static final int[] SKILL_IDS = { 1086, 1204, 1059, 1085, 1078, 1068, 1240, 1077, 1242, 1062, 5739 };
	//private static final int[] SKILL_LEVEL = { 2, 2, 3, 3, 6, 3, 3, 3, 3, 2, 1 };

	public KrateisCubeMatchManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		KrateisCubeEvent cubeEvent = player.getEvent(KrateisCubeEvent.class);
		if(cubeEvent == null)
			return;

		if(command.startsWith("KrateiEnter"))
		{
			if(!cubeEvent.isInProgress())
				showChatWindow(player, 1);
			else
			{

				List<Location> locs = cubeEvent.getObjects(KrateisCubeEvent.TELEPORT_LOCS);

				player.teleToLocation(Rnd.get(locs), ReflectionManager.DEFAULT);
			}
		}
		else if(command.startsWith("KrateiExit"))
			cubeEvent.exitCube(player, true);
	}
}