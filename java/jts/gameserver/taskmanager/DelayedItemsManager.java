package jts.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jts.commons.dbutils.DbUtils;
import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.ItemInstance.ItemLocation;
import jts.gameserver.model.items.PcInventory;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayedItemsManager extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(DelayedItemsManager.class);
	private static DelayedItemsManager _instance;

	private static final Object _lock = new Object();
	private int last_payment_id = 0;

	public static DelayedItemsManager getInstance()
	{
		if(_instance == null)
			_instance = new DelayedItemsManager();
		return _instance;
	}

	public DelayedItemsManager()
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			last_payment_id = get_last_payment_id(con);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}

		ThreadPoolManager.getInstance().schedule(this, 10000L);
	}

	private int get_last_payment_id(Connection con)
	{
		PreparedStatement st = null;
		ResultSet rset = null;
		int result = last_payment_id;
		try
		{
			st = con.prepareStatement("SELECT MAX(payment_id) AS last FROM items_delayed");
			rset = st.executeQuery();
			if(rset.next())
			{
				result = rset.getInt("last");
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(st, rset);
		}
		return result;
	}

	@Override
	public void runImpl() throws Exception
	{
		Player player = null;

		Connection con = null;
		PreparedStatement st = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			int last_payment_id_temp = get_last_payment_id(con);
			if(last_payment_id_temp != last_payment_id)
				synchronized (_lock)
				{
					st = con.prepareStatement("SELECT DISTINCT owner_id FROM items_delayed WHERE payment_status=0 AND payment_id > ?");
					st.setInt(1, last_payment_id);
					rset = st.executeQuery();
					while(rset.next())
					{
						if((player = GameObjectsStorage.getPlayer(rset.getInt("owner_id"))) != null)
						{
							loadDelayed(player, true);
						}
					}
					last_payment_id = last_payment_id_temp;
				}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rset);
		}

		ThreadPoolManager.getInstance().schedule(this, 10000L);
	}

	public int loadDelayed(Player player, boolean notify)
	{
		if(player == null)
			return 0;
		final int player_id = player.getObjectId();
		final PcInventory inv = player.getInventory();
		if(inv == null)
			return 0;

		int restored_counter = 0;

		Connection con = null;
		PreparedStatement st = null, st_delete = null;
		ResultSet rset = null;
		synchronized (_lock)
		{
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				st = con.prepareStatement("SELECT * FROM items_delayed WHERE owner_id=? AND payment_status=0");
				st.setInt(1, player_id);
				rset = st.executeQuery();

				ItemInstance item, newItem;
				st_delete = con.prepareStatement("UPDATE items_delayed SET payment_status=1 WHERE payment_id=?");

				while(rset.next())
				{
					final int ITEM_ID = rset.getInt("item_id");
					final long ITEM_COUNT = rset.getLong("count");
					final int ITEM_ENCHANT = rset.getInt("enchant_level");
					final int PAYMENT_ID = rset.getInt("payment_id");
					final int FLAGS = rset.getInt("flags");
					rset.getInt("attribute");
					rset.getInt("attribute_level");
					boolean stackable = ItemHolder.getInstance().getTemplate(ITEM_ID).isStackable();
					boolean success = false;

					for(int i = 0; i < (stackable ? 1 : ITEM_COUNT); i++)
					{
						item = ItemFunctions.createItem(ITEM_ID);
						if(item.isStackable())
							item.setCount(ITEM_COUNT);
						else
							item.setEnchantLevel(ITEM_ENCHANT);
						item.setLocation(ItemLocation.INVENTORY);
						item.setCustomFlags(FLAGS);

						if(ITEM_COUNT > 0)
						{
							newItem = inv.addItem(item);
							if(newItem == null)
							{
								_log.warn("Unable to delayed create item " + ITEM_ID + " request " + PAYMENT_ID);
								continue;
							}
						}

						success = true;
						restored_counter++;
						if(notify && ITEM_COUNT > 0)
							player.sendPacket(SystemMessage2.obtainItems(ITEM_ID, stackable ? ITEM_COUNT : 1, ITEM_ENCHANT));
					}
					if(!success)
						continue;

					Log.add("<add owner_id=" + player_id + " item_id=" + ITEM_ID + " count=" + ITEM_COUNT + " enchant_level=" + ITEM_ENCHANT + " payment_id=" + PAYMENT_ID + "/>", "delayed_add");

					st_delete.setInt(1, PAYMENT_ID);
					st_delete.execute();
				}
			}
			catch(Exception e)
			{
				_log.error("Could not load delayed items for player " + player + "!", e);
			}
			finally
			{
				DbUtils.closeQuietly(st_delete);
				DbUtils.closeQuietly(con, st, rset);
			}
		}
		return restored_counter;
	}
}