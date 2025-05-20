package npc.model;

import java.util.Map;

import jts.commons.util.Rnd;
import jts.gameserver.cache.Msg;
import jts.gameserver.instancemanager.DimensionalRiftManager;
import jts.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.DelusionChamber;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
@SuppressWarnings("serial")
public final class DelustionGatekeeperInstance extends NpcInstance
{

	public DelustionGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("enterDC"))
		{
			int izId = Integer.parseInt(command.substring(8));
			int type = izId - 120;
			Map<Integer, DimensionalRiftRoom> rooms = DimensionalRiftManager.getInstance().getRooms(type);
			if(rooms == null)
			{
				player.sendPacket(Msg.SYSTEM_ERROR);
				return;
			}

			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(izId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(izId))
			{
				Party party = player.getParty();
				if(party != null)
					new DelusionChamber(party, type, Rnd.get(1, rooms.size() - 1));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}