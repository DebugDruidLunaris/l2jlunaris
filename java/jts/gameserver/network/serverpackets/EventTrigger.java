package jts.gameserver.network.serverpackets;

public class EventTrigger extends L2GameServerPacket
{
	private int _trapId;
	private boolean _active;

	public EventTrigger(int trapId, boolean active)
	{
		_trapId = trapId;
		_active = active;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xCF);
		writeD(_trapId); // trap object id
		writeC(_active ? 1 : 0); // trap activity 1 or 0
	}
}