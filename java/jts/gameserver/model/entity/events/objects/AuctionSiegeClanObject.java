package jts.gameserver.model.entity.events.objects;

import jts.gameserver.model.pledge.Clan;

@SuppressWarnings("serial")
public class AuctionSiegeClanObject extends SiegeClanObject
{
	private long _bid;

	public AuctionSiegeClanObject(String type, Clan clan, long param)
	{
		this(type, clan, param, System.currentTimeMillis());
	}

	public AuctionSiegeClanObject(String type, Clan clan, long param, long date)
	{
		super(type, clan, param, date);
		_bid = param;
	}

	@Override
	public long getParam()
	{
		return _bid;
	}

	public void setParam(long param)
	{
		_bid = param;
	}
}