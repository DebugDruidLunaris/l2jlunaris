package jts.gameserver.network.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(0x38);
	}
}