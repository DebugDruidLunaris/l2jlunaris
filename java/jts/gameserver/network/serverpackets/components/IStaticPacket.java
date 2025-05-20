package jts.gameserver.network.serverpackets.components;

import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.L2GameServerPacket;

public interface IStaticPacket
{
	L2GameServerPacket packet(Player player);
}