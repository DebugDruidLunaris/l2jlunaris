package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.NpcUtils;

@SuppressWarnings("serial")
public final class MaguenTraderInstance extends NpcInstance
{
	public MaguenTraderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_collector"))
		{
			if(Functions.getItemCount(player, 15487) > 0)
				showChatWindow(player, "default/32735-2.htm");
			else
				Functions.addItem(player, 15487, 1);
		}
		else if(command.equalsIgnoreCase("request_maguen"))
		{
			NpcUtils.spawnSingle(18839, Location.findPointToStay(getSpawnedLoc(), 40, 100, getGeoIndex()), getReflection()); // wild maguen
			showChatWindow(player, "default/32735-3.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}