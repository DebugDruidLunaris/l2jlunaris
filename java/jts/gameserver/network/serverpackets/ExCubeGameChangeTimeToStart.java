package jts.gameserver.network.serverpackets;

public class ExCubeGameChangeTimeToStart extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameChangeTimeToStart(int seconds)
	{
		_seconds = seconds;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x97);
		writeD(0x03);

		writeD(_seconds);
	}
}