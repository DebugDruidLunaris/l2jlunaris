package jts.gameserver.network.serverpackets;

public class ExDissmissMpccRoom extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExDissmissMpccRoom();

	@Override
	protected void writeImpl()
	{
		writeEx(0x9D);
	}
}