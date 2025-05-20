package npc.model.residences;

import jts.gameserver.model.Player;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class TeleportSiegeGuardInstance extends SiegeGuardInstance
{
	public TeleportSiegeGuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;
	}
}