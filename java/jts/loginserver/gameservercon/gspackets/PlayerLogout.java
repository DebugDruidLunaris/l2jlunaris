package jts.loginserver.gameservercon.gspackets;

import jts.loginserver.gameservercon.GameServer;
import jts.loginserver.gameservercon.ReceivablePacket;

public class PlayerLogout extends ReceivablePacket
{
	private String account;

	@Override
	protected void readImpl()
	{
		account = readS();
	}

	@Override
	protected void runImpl()
	{
		GameServer gs = getGameServer();
		if(gs.isAuthed())
			gs.removeAccount(account);
	}
}
