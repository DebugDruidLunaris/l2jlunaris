package jts.gameserver.network.serverpackets;

public class ExGoodsInventoryChangedNotify extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExGoodsInventoryChangedNotify();

	@Override
	protected void writeImpl()
	{
		writeEx(0xE2);
	}
}