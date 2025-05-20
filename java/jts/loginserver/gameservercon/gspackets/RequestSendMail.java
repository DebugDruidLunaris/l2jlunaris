package jts.loginserver.gameservercon.gspackets;

import jts.loginserver.gameservercon.ReceivablePacket;
import jts.loginserver.mail.MailSystem;

/**
 * @author mrTJO
 */
public class RequestSendMail extends ReceivablePacket
{
	String _accountName, _mailId;
	String[] _args;

	@Override
	protected void readImpl()
	{
		_accountName = readS();
		_mailId = readS();
		int argNum = readC();
		_args = new String[argNum];
		for(int i = 0; i < argNum; i++)
			_args[i] = readS();
	}

	@Override
	protected void runImpl()
	{
		MailSystem.getInstance().sendMail(_accountName, _mailId, _args);
	}
}
