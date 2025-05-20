package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.Henna;
import jts.gameserver.utils.Log_New;

public class RequestHennaUnequip extends L2GameClientPacket
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

		for(int i = 1; i <= 3; i++)
		{
			Henna henna = player.getHenna(i);
			if(henna == null)
				continue;

			if(henna.getSymbolId() == _symbolId)
			{
				long price = henna.getPrice() / 5;
				if(player.getAdena() < price)
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					break;
				}

				player.reduceAdena(price);

				player.removeHenna(i);
				Log_New.LogEvent(player.getName(), "Henna", "RemoveHena", new String[] { "Removed Hena ID " + this._symbolId + "" });
				player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
				break;
			}
		}
	}
}