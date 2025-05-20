package jts.gameserver.network.serverpackets;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExShowVariationMakeWindow();

	@Override
	protected final void writeImpl()
	{
		writeEx(0x51);
	}
}