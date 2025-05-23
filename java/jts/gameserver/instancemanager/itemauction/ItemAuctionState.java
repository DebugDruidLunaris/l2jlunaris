package jts.gameserver.instancemanager.itemauction;

import jts.commons.lang.ArrayUtils;

public enum ItemAuctionState
{
	CREATED,
	STARTED,
	FINISHED;

	public static final ItemAuctionState stateForStateId(int stateId)
	{
		return ArrayUtils.valid(values(), stateId);
	}
}