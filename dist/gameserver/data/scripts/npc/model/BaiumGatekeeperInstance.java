package npc.model;

import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import bosses.BaiumManager;

@SuppressWarnings("serial")
public final class BaiumGatekeeperInstance extends NpcInstance
{
	private static final int Baium = 29020;
	private static final int BaiumNpc = 29025;
	private static final int BloodedFabric = 4295;
	private static final Location TELEPORT_POSITION = new Location(113100, 14500, 10077);

	public BaiumGatekeeperInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("request_entrance"))
		{
			if(ItemFunctions.getItemCount(player, BloodedFabric) > 0)
			{
				NpcInstance baiumBoss = GameObjectsStorage.getByNpcId(Baium);
				if(baiumBoss != null)
				{
					showChatWindow(player, "default/31862-1.htm");
					return;
				}
				NpcInstance baiumNpc = GameObjectsStorage.getByNpcId(BaiumNpc);
				if(baiumNpc == null)
				{
					showChatWindow(player, "default/31862-2.htm");
					return;
				}
				ItemFunctions.removeItem(player, BloodedFabric, 1, true);
				player.setVar("baiumPermission", "granted", -1);
				player.teleToLocation(TELEPORT_POSITION);
			}
			else
			{
				showChatWindow(player, "default/31862-3.htm");
			}
		}
		else if(command.startsWith("request_wakeup"))
		{
			if(player.getVar("baiumPermission") == null || !player.getVar("baiumPermission").equalsIgnoreCase("granted"))
			{
				showChatWindow(player, "default/29025-1.htm");
				return;
			}
			if(isBusy())
			{
				showChatWindow(player, "default/29025-2.htm");
			}
			setBusy(true);
			Functions.npcSay(this, "You called my name! Now you gonna die!");
			BaiumManager.spawnBaium(this, player);
		}
		else
			super.onBypassFeedback(player, command);
	}
}