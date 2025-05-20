package jts.gameserver.network.serverpackets;

/**
 * sample
 * <p>
 * 4b
 * c1 b2 e0 4a
 * 00 00 00 00
 * <p>
 * format
 * cdd
 */
public class AskJoinParty extends L2GameServerPacket
{
	private String _requestorName;
	private int _itemDistribution;

	/**
	 * @param int objectId of the target
	 * @param int
	 */
	public AskJoinParty(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x39);
		writeS(_requestorName);
		writeD(_itemDistribution);
	}
}