package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.actor.instances.player.ShortCut;

public class ShortCutRegister extends ShortCutPacket
{
	private ShortcutInfo _shortcutInfo;

	public ShortCutRegister(Player player, ShortCut sc)
	{
		_shortcutInfo = convert(player, sc);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x44);

		_shortcutInfo.write(this);
	}
}