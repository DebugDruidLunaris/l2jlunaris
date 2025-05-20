package npc.model;

import instances.Frintezza;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.ReflectionUtils;

@SuppressWarnings("serial")
public final class FrintezzaGatekeeperInstance extends NpcInstance
{
	private static final int frintezzaIzId = 136;

	public FrintezzaGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_frintezza"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(frintezzaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(frintezzaIzId))
			{
				if(ItemFunctions.removeItem(player, 8073, 1, true) < 1)
				{
					player.sendPacket(new SystemMessage(SystemMessage.C1S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
					return;
				}
				ReflectionUtils.enterReflection(player, new Frintezza(), frintezzaIzId);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}