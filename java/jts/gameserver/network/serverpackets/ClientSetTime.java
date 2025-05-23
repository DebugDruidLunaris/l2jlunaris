package jts.gameserver.network.serverpackets;

import jts.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ClientSetTime();

	@Override
	protected final void writeImpl()
	{
		writeC(0xf2);
		writeD(GameTimeController.getInstance().getGameTime()); // time in client minutes
		writeD(6); //constant to match the server time( this determines the speed of the client clock)
	}
}