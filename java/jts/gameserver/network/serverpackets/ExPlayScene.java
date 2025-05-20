package jts.gameserver.network.serverpackets;

public class ExPlayScene extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x5c);
		writeD(0x00); //Kamael
	}
}