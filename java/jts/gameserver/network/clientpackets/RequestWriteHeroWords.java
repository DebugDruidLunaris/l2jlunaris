package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Hero;

public class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;

	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null || !player.isHero())
			return;

		if(_heroWords == null || _heroWords.length() > 300)
			return;

		Hero.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
	}
}