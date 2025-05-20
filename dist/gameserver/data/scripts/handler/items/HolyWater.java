package handler.items;

import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import npc.model.HellboundRemnantInstance;

public class HolyWater extends SimpleItemHandler
{
	private static final int[] ITEM_IDS = new int[] { 9673 };

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		GameObject target = player.getTarget();

		if(target == null || !(target instanceof HellboundRemnantInstance))
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		HellboundRemnantInstance npc = (HellboundRemnantInstance) target;
		if(npc.isDead())
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		player.broadcastPacket(new MagicSkillUse(player, npc, 2358, 1, 0, 0));
		npc.onUseHolyWater(player);

		return true;
	}
}