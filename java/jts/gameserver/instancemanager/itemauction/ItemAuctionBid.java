package jts.gameserver.instancemanager.itemauction;

import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;

public final class ItemAuctionBid
{
	private final int _charId;
	private long _lastBid;

	public ItemAuctionBid(int charId, long lastBid)
	{
		this._charId = charId;
		this._lastBid = lastBid;
	}

	public final int getCharId()
	{
		return this._charId;
	}

	public final long getLastBid()
	{
		return this._lastBid;
	}

	final void setLastBid(long lastBid)
	{
		this._lastBid = lastBid;
	}

	final void cancelBid()
	{
		this._lastBid = -1L;
	}

	final boolean isCanceled()
	{
		return this._lastBid == -1L;
	}

	final Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(this._charId);
	}
}