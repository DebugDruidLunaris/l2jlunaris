package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.HennaHolder;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.HennaItemInfo;
import jts.gameserver.templates.Henna;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	// format  cd
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Henna henna = HennaHolder.getInstance().getHenna(_symbolId);
		if(henna != null)
			player.sendPacket(new HennaItemInfo(henna, player));
	}
}