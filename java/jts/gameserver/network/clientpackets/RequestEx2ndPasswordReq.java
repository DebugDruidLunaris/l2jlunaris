package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.network.SecondaryPasswordAuth;
import jts.gameserver.network.serverpackets.Ex2ndPasswordAck;

public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private int _changePass;
	private String _password, _newPassword;

	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if(_changePass == 2)
			_newPassword = readS();
	}

	@Override
	protected void runImpl()
	{
		if(!Config.SECOND_AUTH_ENABLED)
			return;

		SecondaryPasswordAuth spa = getClient().getSecondaryAuth();
		boolean exVal = false;

		if(_changePass == 0 && !spa.passwordExist())
			exVal = spa.savePassword(_password);
		else if(_changePass == 2 && spa.passwordExist())
			exVal = spa.changePassword(_password, _newPassword);

		if(exVal)
			getClient().sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.SUCCESS));
	}
}