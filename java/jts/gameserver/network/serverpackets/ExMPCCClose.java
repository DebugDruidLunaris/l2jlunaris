package jts.gameserver.network.serverpackets;

public class ExMPCCClose extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExMPCCClose();

	@Override
	protected void writeImpl()
	{
		writeEx(0x13);
	}
}
