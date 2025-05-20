package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExReplyPostItemList;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class RequestExPostItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl() {} //just a trigger

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
			activeChar.sendActionFailed();

		if(!Config.ALLOW_MAIL)
		{
			activeChar.sendMessage(new CustomMessage("mail.Disabled", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new ExReplyPostItemList(activeChar));
	}
}