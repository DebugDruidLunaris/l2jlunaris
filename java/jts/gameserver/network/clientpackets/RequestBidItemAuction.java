package jts.gameserver.network.clientpackets;

import jts.gameserver.instancemanager.itemauction.ItemAuction;
import jts.gameserver.instancemanager.itemauction.ItemAuctionInstance;
import jts.gameserver.instancemanager.itemauction.ItemAuctionManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;

public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		ItemInstance adena = activeChar.getInventory().getItemByItemId(57);
		if(_bid < 0 || _bid > adena.getCount())
			return;

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		NpcInstance broker = activeChar.getLastNpc();
		if(broker == null || broker.getNpcId() != _instanceId || activeChar.getDistance(broker.getX(), broker.getY()) > Creature.INTERACTION_DISTANCE)
			return;
		if(instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if(auction != null)
				auction.registerBid(activeChar, _bid);
		}
	}
}