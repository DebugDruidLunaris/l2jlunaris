package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.network.serverpackets.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		if(!Config.SECOND_AUTH_ENABLED || getClient().getSecondaryAuth().isAuthed())
		{
			sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_OK));
			return;
		}
		getClient().getSecondaryAuth().openDialog();
	}
}