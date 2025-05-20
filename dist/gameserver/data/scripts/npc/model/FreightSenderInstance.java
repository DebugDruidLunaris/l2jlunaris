package npc.model;

import jts.gameserver.model.Player;
import jts.gameserver.model.instances.MerchantInstance;
import jts.gameserver.network.serverpackets.PackageToList;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.WarehouseFunctions;

@SuppressWarnings("serial")
public class FreightSenderInstance extends MerchantInstance
{
	public FreightSenderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("deposit_items"))
			player.sendPacket(new PackageToList(player));
		else if(command.equalsIgnoreCase("withdraw_items"))
			WarehouseFunctions.showFreightWindow(player);
		else
			super.onBypassFeedback(player, command);
	}
}