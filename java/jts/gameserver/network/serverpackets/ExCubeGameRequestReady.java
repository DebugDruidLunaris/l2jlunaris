package jts.gameserver.network.serverpackets;

public class ExCubeGameRequestReady extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x97);
		writeD(0x04);
	}
}