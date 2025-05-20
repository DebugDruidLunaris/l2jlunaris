package jts.gameserver.network.clientpackets;

class SuperCmdSummonCmd extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private String _summonName;

	/**
	 * @param buf
	 * @param client
	 */
	@Override
	protected void readImpl()
	{
		_summonName = readS();
	}

	@Override
	protected void runImpl() {}
}