package jts.gameserver.network.serverpackets;

public class ExResponseShowContents extends L2GameServerPacket
{
	private final String _contents;

	public ExResponseShowContents(String contents)
	{
		_contents = contents;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xB0);
		writeS(_contents);
	}
}