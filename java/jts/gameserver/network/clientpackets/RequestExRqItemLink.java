package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.ItemInfoCache;
import jts.gameserver.model.items.ItemInfo;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		ItemInfo item;
		if((item = ItemInfoCache.getInstance().get(_objectId)) == null)
			sendPacket(ActionFail.STATIC);
		else
			sendPacket(new ExRpItemLink(item));
	}
}