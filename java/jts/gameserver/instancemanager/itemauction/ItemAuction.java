package jts.gameserver.instancemanager.itemauction;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemAuction
{
	private static Logger _log = LoggerFactory.getLogger(ItemAuction.class);
	private static long ENDING_TIME_EXTEND_5 = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES);
	private static long ENDING_TIME_EXTEND_8 = TimeUnit.MILLISECONDS.convert(8L, TimeUnit.MINUTES);
	private int _auctionId;
	private int _instanceId;
	private long _startingTime;
	private long _endingTime;
	private AuctionItem _auctionItem;
	private TIntObjectHashMap<ItemAuctionBid> _auctionBids;
	private ItemAuctionState _auctionState;
	private int _scheduledAuctionEndingExtendState;
	private int _auctionEndingExtendState;
	private ItemAuctionBid _highestBid;
	private int _lastBidPlayerObjId;

	public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem, ItemAuctionState auctionState)
	{
		this._auctionId = auctionId;
		this._instanceId = instanceId;
		this._startingTime = startingTime;
		this._endingTime = endingTime;
		this._auctionItem = auctionItem;
		//Testar
		this._auctionBids = new TIntObjectHashMap<ItemAuctionBid>();
		this._auctionState = auctionState;
	}

	void addBid(ItemAuctionBid bid)
	{
		this._auctionBids.put(bid.getCharId(), bid);
		if ((this._highestBid == null) || (this._highestBid.getLastBid() < bid.getLastBid())) 
		{
			this._highestBid = bid;
		}
	}

	public ItemAuctionState getAuctionState()
	{
		return this._auctionState;
	}
  
	public synchronized boolean setAuctionState(ItemAuctionState expected, ItemAuctionState wanted)
	{
		if (this._auctionState != expected) 
		{
			return false;
		}
		this._auctionState = wanted;
    	store();
    	return true;
	}

	public int getAuctionId()
	{
		return this._auctionId;
	}

	public int getInstanceId()
	{
		return this._instanceId;
	}

	public AuctionItem getAuctionItem()
	{
		return this._auctionItem;
	}

	public ItemInstance createNewItemInstance()
	{
		return this._auctionItem.createNewItemInstance();
	}

	public long getAuctionInitBid()
	{
		return this._auctionItem.getAuctionInitBid();
	}

	public ItemAuctionBid getHighestBid()
	{
		return this._highestBid;
	}

	public int getAuctionEndingExtendState()
	{
		return this._auctionEndingExtendState;
	}

	public int getScheduledAuctionEndingExtendState()
	{
		return this._scheduledAuctionEndingExtendState;
	}

	public void setScheduledAuctionEndingExtendState(int state)
	{
		this._scheduledAuctionEndingExtendState = state;
	}

	public long getStartingTime()
	{
		return this._startingTime;
	}

	public long getEndingTime()
	{
		if (this._auctionEndingExtendState == 0) 
		{
			return this._endingTime;
		}
		if (this._auctionEndingExtendState == 1) 
		{
			return this._endingTime + ENDING_TIME_EXTEND_5;
		}
		return this._endingTime + ENDING_TIME_EXTEND_8;
	}

	public long getStartingTimeRemaining()
	{
		return Math.max(getEndingTime() - System.currentTimeMillis(), 0L);
	}

	public long getFinishingTimeRemaining()
	{
		return Math.max(getEndingTime() - System.currentTimeMillis(), 0L);
	}

	public void store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO item_auction (auctionId,instanceId,auctionItemId,startingTime,endingTime,auctionStateId) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE auctionStateId=?");
			statement.setInt(1, this._auctionId);
			statement.setInt(2, this._instanceId);
			statement.setInt(3, this._auctionItem.getAuctionItemId());
			statement.setLong(4, this._startingTime);
			statement.setLong(5, this._endingTime);
			statement.setInt(6, this._auctionState.ordinal());
			statement.setInt(7, this._auctionState.ordinal());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		_log.warn("", e);
		}
		finally
		{
		DbUtils.closeQuietly(con, statement);
		}
	}

	public int getAndSetLastBidPlayerObjectId(int playerObjId)
	{
		int lastBid = this._lastBidPlayerObjId;
		this._lastBidPlayerObjId = playerObjId;
		return lastBid;
	}

	public void updatePlayerBid(ItemAuctionBid bid, boolean delete)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if (delete)
			{
				statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=? AND playerObjId=?");
				statement.setInt(1, this._auctionId);
				statement.setInt(2, bid.getCharId());
			}
		else
		{
			statement = con.prepareStatement("INSERT INTO item_auction_bid (auctionId,playerObjId,playerBid) VALUES (?,?,?) ON DUPLICATE KEY UPDATE playerBid=?");
			statement.setInt(1, this._auctionId);
			statement.setInt(2, bid.getCharId());
			statement.setLong(3, bid.getLastBid());
			statement.setLong(4, bid.getLastBid());
		}
		statement.execute();
		statement.close();
		}
		catch (SQLException e)
		{
		_log.warn("", e);
		}
		finally
		{
		DbUtils.closeQuietly(con, statement);
		}
	}

	public void registerBid(Player player, long newBid)
	{
		if (player == null) 
		{
			throw new NullPointerException();
		}
		if (newBid < getAuctionInitBid())
		{
			player.sendPacket(Msg.YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_THAT_CAN_BE_BID);
			return;
		}
		if (newBid > Config.ALT_ITEM_AUCTION_MAX_BID)
		{
			if (Config.ALT_ITEM_AUCTION_MAX_BID == 100000000000L) 
			{
				player.sendPacket(Msg.YOUR_BID_CANNOT_EXCEED_100_BILLION);
			} 
			else 
			{
				player.sendMessage(new CustomMessage("common.ItemAuction.LimitExceeded", player, new Object[0]).addNumber(Config.ALT_ITEM_AUCTION_MAX_BID));
			}
			return;
		}
		if (getAuctionState() != ItemAuctionState.STARTED) 
		{
			return;
		}
		int charId = player.getObjectId();
		synchronized (this._auctionBids)
		{
			if ((this._highestBid != null) && (newBid < this._highestBid.getLastBid()))
			{
				player.sendPacket(Msg.YOUR_BID_MUST_BE_HIGHER_THAN_THE_CURRENT_HIGHEST_BID);
				return;
			}
			ItemAuctionBid bid = getBidFor(charId);
			if (bid == null)
			{
				if (!reduceItemCount(player, newBid)) 
				{
					return;
				}
				bid = new ItemAuctionBid(charId, newBid);
				this._auctionBids.put(charId, bid);

				onPlayerBid(player, bid);
				updatePlayerBid(bid, false);

				player.sendPacket(new SystemMessage(678).addNumber(newBid));
				return;
			}
			if (!Config.ALT_ITEM_AUCTION_CAN_REBID)
			{
				player.sendPacket(Msg.SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME);
				return;
			}
			if (bid.getLastBid() >= newBid)
			{
				player.sendPacket(Msg.THE_SECOND_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_ORIGINAL);
				return;
			}
			if (!reduceItemCount(player, newBid - bid.getLastBid())) 
			{
				return;
			}
			bid.setLastBid(newBid);
			onPlayerBid(player, bid);
			updatePlayerBid(bid, false);

			player.sendPacket(new SystemMessage(678).addNumber(newBid));
		}
	}

	private void onPlayerBid(Player player, ItemAuctionBid bid)
	{
		if (this._highestBid == null)
		{
			this._highestBid = bid;
		}
		else if (this._highestBid.getLastBid() < bid.getLastBid())
		{
			Player old = this._highestBid.getPlayer();
			if (old != null) 
			{
				old.sendPacket(Msg.YOU_HAVE_BEEN_OUTBID);
			}
			this._highestBid = bid;
		}
		if (getEndingTime() - System.currentTimeMillis() <= 600000L) 
		{
			if (this._auctionEndingExtendState == 0)
			{
				this._auctionEndingExtendState = 1;
				broadcastToAllBidders(Msg.BIDDER_EXISTS__THE_AUCTION_TIME_HAS_BEEN_EXTENDED_BY_5_MINUTES);
			}
			else if ((this._auctionEndingExtendState == 1) && (getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId()))
			{
				this._auctionEndingExtendState = 2;
				broadcastToAllBidders(Msg.BIDDER_EXISTS__AUCTION_TIME_HAS_BEEN_EXTENDED_BY_3_MINUTES);
			}
		}
	}

	public void broadcastToAllBidders(L2GameServerPacket packet)
	{
		TIntObjectIterator<ItemAuctionBid> itr = this._auctionBids.iterator();
		ItemAuctionBid bid = null;
		while (itr.hasNext())
		{
			itr.advance();
			bid = (ItemAuctionBid)itr.value();
			Player player = bid.getPlayer();
			if (player != null) 
			{
				player.sendPacket(packet);
			}
		}
	}

	public void cancelBid(Player player)
	{
		if (player == null) 
		{
			throw new NullPointerException();
		}
		switch (getAuctionState())
		{
			case CREATED: 
				player.sendPacket(Msg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU);
				return;
			case FINISHED:
				if (this._startingTime < System.currentTimeMillis() - Config.ALT_ITEM_AUCTION_MAX_CANCEL_TIME_IN_MILLIS)
				{
					player.sendPacket(Msg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU); 
					return;
				}      
				break;
			default:
				break;
		}
		int charId = player.getObjectId();
		synchronized (this._auctionBids)
		{
			if (this._highestBid == null)
			{
				player.sendPacket(Msg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU);
				return;
			}
			ItemAuctionBid bid = getBidFor(charId);
			if ((bid == null) || (bid.isCanceled()))
			{
				player.sendPacket(Msg.THERE_ARE_NO_FUNDS_PRESENTLY_DUE_TO_YOU);
				return;
			}
			if (bid.getCharId() == this._highestBid.getCharId())
			{
				player.sendMessage(new CustomMessage("common.ItemAuction.CancelBid", player, new Object[0]));
				return;
			}
			increaseItemCount(player, bid.getLastBid());
			player.sendPacket(Msg.YOU_HAVE_CANCELED_YOUR_BID);
			if (Config.ALT_ITEM_AUCTION_CAN_REBID)
			{
				this._auctionBids.remove(charId);
				updatePlayerBid(bid, true);
			}
			else
			{
				bid.cancelBid();
				updatePlayerBid(bid, false);
			}
		}
	}

	private boolean reduceItemCount(Player player, long count)
	{
		if ((Config.ALT_ITEM_AUCTION_BID_ITEM_ID == 57) || (!getAuctionItem().isEquipped()))
		{
			if (!player.reduceAdena(count, true))
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA_FOR_THIS_BID);
				return false;
			}
			return true;
		}
		return player.getInventory().destroyItemByItemId(Config.ALT_ITEM_AUCTION_BID_ITEM_ID, count);
	}

	private void increaseItemCount(Player player, long count)
	{
		if ((Config.ALT_ITEM_AUCTION_BID_ITEM_ID == 57) || (!getAuctionItem().isEquipped())) 
		{
			player.addAdena(count);
		} 
		else 
		{
			player.getInventory().addItem(Config.ALT_ITEM_AUCTION_BID_ITEM_ID, count);
		}
		player.sendPacket(SystemMessage2.obtainItems(Config.ALT_ITEM_AUCTION_BID_ITEM_ID, count, 0));
	}

	public long getLastBid(Player player)
	{
		ItemAuctionBid bid = getBidFor(player.getObjectId());
		return bid != null ? bid.getLastBid() : -1L;
	}

	public ItemAuctionBid getBidFor(int charId)
	{
		return (ItemAuctionBid)this._auctionBids.get(charId);
	}
}