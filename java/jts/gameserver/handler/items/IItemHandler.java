package jts.gameserver.handler.items;

import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Mother class of all itemHandlers.<BR><BR>
 * an IItemHandler implementation has to be stateless
 */
public interface IItemHandler
{
	public static final IItemHandler NULL = new IItemHandler(){
		@Override
		public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
		{
			return false;
		}

		@Override
		public void dropItem(Player player, ItemInstance item, long count, Location loc)
		{
			if(item.isEquipped())
			{
				player.getInventory().unEquipItem(item);
				player.sendUserInfo(true);
			}

			item = player.getInventory().removeItemByObjectId(item.getObjectId(), count);
			if(item == null)
			{
				player.sendActionFailed();
				return;
			}

			Log.LogItem(player, Log.Drop, item);
			Log_New.LogEvent(player.getName(), player.getIP(), "DroppedItems", new String[] { "player drop item: " + item.getName() + " to loc " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + "" });
			item.dropToTheGround(player, loc);
			player.disableDrop(1000);

			player.sendChanges();
		}

		@Override
		public boolean pickupItem(Playable playable, ItemInstance item)
		{
			return true;
		}

		@Override
		public int[] getItemIds()
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
	};

	/**
	 * Launch task associated to the item.
	 * @param playable
	 * @param item : L2ItemInstance designating the item to use
	 * @param ctrl
	 */
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl);

	/**
	 * Check can drop or not
	 * @param playable
	 * @param item
	 * @param count
	 *@param loc  @return can drop
	 */
	public void dropItem(Player player, ItemInstance item, long count, Location loc);

	/**
	 * Check if can pick up item
	 * @param playable
	 * @param item
	 * @return
	 */
	public boolean pickupItem(Playable playable, ItemInstance item);

	/**
	 * Returns the list of item IDs corresponding to the type of item.<BR><BR>
	 * <B><I>Use :</I></U><BR>
	 * This method is called at initialization to register all the item IDs automatically
	 * @return int[] designating all itemIds for a type of item.
	 */
	public int[] getItemIds();
}