package jts.gameserver.listener.actor.player.impl;

import jts.commons.lang.reference.HardReference;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.Location;

public class SummonAnswerListener implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private Location _location;
	private long _count;

	public SummonAnswerListener(Player player, Location loc, long count)
	{
		_playerRef = player.getRef();
		_location = loc;
		_count = count;
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;

		player.abortAttack(true, true);
		player.abortCast(true, true);
		player.stopMove();
		if(_count > 0)
		{
			if(player.getInventory().destroyItemByItemId(8615, _count))
			{
				player.sendPacket(SystemMessage2.removeItems(8615, _count));
				player.teleToLocation(_location);
			}
			else
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		else
			player.teleToLocation(_location);
	}

	@Override
	public void sayNo() {}
}