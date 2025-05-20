package jts.gameserver.loginservercon.lspackets;

import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.ReceivablePacket;
import jts.gameserver.model.Player;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class ChangePasswordResponse extends ReceivablePacket
{
	String account;
	boolean changed;

	@Override
	public void readImpl()
	{
		account = readS();
		changed = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		GameClient client = LoginServerCommunication.getInstance().getAuthedClient(account);
		if(client == null)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(changed)
		{
			String result = new CustomMessage("communityboard.cabinet.password.result.true", activeChar).toString();
			activeChar.setPasswordResult("<font color=\"33CC33\">" + result + "</font>");
			activeChar.sendMessage(result);
		}
		else
		{
			String result = new CustomMessage("communityboard.cabinet.password.result.false", activeChar).toString();
			activeChar.setPasswordResult("<font color=\"33CC33\">" + result + "</font>");
			activeChar.sendMessage(result);
		}
	}
}
