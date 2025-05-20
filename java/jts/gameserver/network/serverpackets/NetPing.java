package jts.gameserver.network.serverpackets;

public class NetPing extends L2GameServerPacket
{
	private int _time;

	public NetPing(int time)
	{
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xD9);
		writeD(_time);
	}
}
