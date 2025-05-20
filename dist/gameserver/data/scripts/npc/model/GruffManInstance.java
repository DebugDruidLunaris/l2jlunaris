package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.ReflectionUtils;

@SuppressWarnings("serial")
public final class GruffManInstance extends NpcInstance
{
	private static final int elcardiaIzId = 158;

	public GruffManInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("elcardia_enter"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(elcardiaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(elcardiaIzId))
				ReflectionUtils.enterReflection(player, elcardiaIzId);
		}
		else
			super.onBypassFeedback(player, command);
	}
}