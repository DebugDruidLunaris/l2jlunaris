package jts.gameserver.loginservercon.lspackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.model.Player;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.ServerClose;

public class KickPlayer extends ReceivablePacket
{
	String account;

	@Override
	public void readImpl()
	{
		account = readS();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = LoginServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			client = LoginServerCommunication.getInstance().removeAuthedClient(account);
		if(client == null)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar != null)
		{
			//FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
			activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
			activeChar.kick();
		}
		else
			client.close(ServerClose.STATIC);
	}
}