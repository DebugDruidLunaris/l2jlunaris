package jts.gameserver.network.clientpackets;

import jts.gameserver.model.PcCafe;
import jts.gameserver.model.Player;

public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	private String couponCode;

	@Override
	protected void readImpl()
	{
		couponCode = readS();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		else
			PcCafe.requestEnterCode(player, couponCode);
	}
}