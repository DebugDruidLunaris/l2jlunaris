package jts.gameserver.network.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(0x44);
	}
}