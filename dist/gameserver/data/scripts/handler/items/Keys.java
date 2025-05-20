package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.DoorHolder;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.DoorTemplate;

public class Keys extends ScriptItemHandler
{
	private int[] _itemIds = null;

	public Keys()
	{
		TIntHashSet keys = new TIntHashSet();
		for(DoorTemplate door : DoorHolder.getInstance().getDoors().values())
			if(door != null && door.getKey() > 0)
				keys.add(door.getKey());
		_itemIds = keys.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = playable.getPlayer();
		GameObject target = player.getTarget();
		if(target == null || !target.isDoor())
		{
			player.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		DoorInstance door = (DoorInstance) target;
		if(door.isOpen())
		{
			player.sendPacket(Msg.IT_IS_NOT_LOCKED);
			return false;
		}
		if(door.getKey() <= 0 || item.getItemId() != door.getKey()) // ключ не подходит к двери
		{
			player.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}
		if(player.getDistance(door) > 300)
		{
			player.sendPacket(Msg.YOU_CANNOT_CONTROL_BECAUSE_YOU_ARE_TOO_FAR);
			return false;
		}
		if(!player.getInventory().destroyItem(item, 1L))
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return false;
		}
		player.sendPacket(SystemMessage2.removeItems(item.getItemId(), 1));
		player.sendMessage(new CustomMessage("jts.gameserver.skills.skillclasses.Unlock.Success", player));
		door.openMe(player, true);
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}