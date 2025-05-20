package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.HennaHolder;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.HennaUnequipInfo;
import jts.gameserver.templates.Henna;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * format: d
	 */
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
			player.sendPacket(new HennaUnequipInfo(henna, player));
	}
}