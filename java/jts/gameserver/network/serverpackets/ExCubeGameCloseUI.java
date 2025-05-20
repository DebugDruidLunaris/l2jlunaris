package jts.gameserver.network.serverpackets;

public class ExCubeGameCloseUI extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameCloseUI()
	{}

	@Override
	protected void writeImpl()
	{
		writeEx(0x97);
		writeD(0xffffffff);
	}
}