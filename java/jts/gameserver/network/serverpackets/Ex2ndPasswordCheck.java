package jts.gameserver.network.serverpackets;

public class Ex2ndPasswordCheck extends L2GameServerPacket
{
	public static final int PASSWORD_NEW = 0x00;
	public static final int PASSWORD_PROMPT = 0x01;
	public static final int PASSWORD_OK = 0x02;

	int _windowType;

	public Ex2ndPasswordCheck(int windowType)
	{
		_windowType = windowType;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xE5);
		writeD(_windowType);
		writeD(0x00);
	}
}