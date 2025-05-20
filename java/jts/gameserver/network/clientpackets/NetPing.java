package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;

public class NetPing extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _time, _unk1, _unk2;

	@Override
	protected void readImpl()
	{
		_time = readD();
		_unk1 = readD();
		_unk2 = readD();
	}

	@Override
	protected void runImpl()
	{
    	Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
        	return;
	}
}