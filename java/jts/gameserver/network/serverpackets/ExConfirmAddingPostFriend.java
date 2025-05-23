package jts.gameserver.network.serverpackets;

public class ExConfirmAddingPostFriend extends L2GameServerPacket
{
	public static int NAME_IS_NOT_EXISTS = 0;
	public static int SUCCESS = 1;
	public static int PREVIOS_NAME_IS_BEEN_REGISTERED = -1; // The previous name is being registered. Please try again later.
	public static int NAME_IS_NOT_EXISTS2 = -2;
	public static int LIST_IS_FULL = -3;
	public static int ALREADY_ADDED = -4;
	public static int NAME_IS_NOT_REGISTERED = -4;

	private String _name;
	private int _result;

	public ExConfirmAddingPostFriend(String name, int s)
	{
		_name = name;
		_result = s;
	}

	@Override
	public void writeImpl()
	{
		writeEx(0xD2);
		writeS(_name);
		writeD(_result);
	}
}