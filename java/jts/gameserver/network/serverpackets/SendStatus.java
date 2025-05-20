package jts.gameserver.network.serverpackets;

import jts.gameserver.Config;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;

public final class SendStatus extends L2GameServerPacket
{
	private static final long MIN_UPDATE_PERIOD = 30000;
	private static int online_players = 0;
	private static int max_online_players = 0;
	private static int online_priv_store = 0;
	private static long last_update = 0;

	public SendStatus()
	{
		if(System.currentTimeMillis() - last_update < MIN_UPDATE_PERIOD)
			return;
		last_update = System.currentTimeMillis();
		int i = 0;
		int j = 0;
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			i++;
			if(player.isInStoreMode() && (!Config.SENDSTATUS_TRADE_JUST_OFFLINE || player.isInOfflineMode()))
				j++;
		}
		online_players = i;
		online_priv_store = (int) Math.floor(j * Config.SENDSTATUS_TRADE_MOD);
		max_online_players = Math.max(max_online_players, online_players);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2E); // Packet ID
		writeD(0x01); // World ID
		writeD(max_online_players); // Max Online
		writeD(online_players + 2); // Current Online
		writeD(online_players); // Current Online
		writeD(online_priv_store); // Priv.Store Chars

		// SEND TRASH
		writeH(0x30);
		writeH(0x2C);
		writeH(0x35);
		writeH(0x31);
		writeH(0x30);
		writeH(0x2C);
		writeH(0x37);
		writeH(0x37);
		writeH(0x37);
		writeH(0x35);
		writeH(0x38);
		writeH(0x2C);
		writeH(0x36);
		writeH(0x35);
		writeH(0x30);
		writeD(0x36);
		writeD(0x77);
		writeD(0xB7);
		writeQ(0x9F);
		writeD(0x00);
		writeH(0x41);
		writeH(0x75);
		writeH(0x67);
		writeH(0x20);
		writeH(0x32);
		writeH(0x39);
		writeH(0x20);
		writeH(0x32);
		writeH(0x30);
		writeH(0x30);
		writeD(0x39);
		writeH(0x30);
		writeH(0x32);
		writeH(0x3A);
		writeH(0x34);
		writeH(0x30);
		writeH(0x3A);
		writeH(0x34);
		writeD(0x33);
		writeD(0x57);
		writeC(0x11);
		writeC(0x5D);
		writeC(0x1F);
		writeC(0x60);
	}
}