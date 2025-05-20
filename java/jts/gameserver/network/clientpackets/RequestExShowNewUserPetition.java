package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExResponseShowStepOne;

public class RequestExShowNewUserPetition extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_NEW_PETITION_SYSTEM)
			return;

		player.sendPacket(new ExResponseShowStepOne(player));
	}
}