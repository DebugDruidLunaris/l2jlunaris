package jts.gameserver.network.serverpackets;

public class ExDominionWarEnd extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExDominionWarEnd();

	@Override
	public void writeImpl()
	{
		writeEx(0xA4);
	}
}