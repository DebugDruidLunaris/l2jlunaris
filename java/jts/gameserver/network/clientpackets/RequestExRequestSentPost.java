package jts.gameserver.network.clientpackets;

import jts.gameserver.dao.MailDAO;
import jts.gameserver.model.Player;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.network.serverpackets.ExReplySentPost;
import jts.gameserver.network.serverpackets.ExShowSentPostList;

public class RequestExRequestSentPost extends L2GameClientPacket
{
	private int postId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		postId = readD(); // id письма
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			activeChar.sendPacket(new ExReplySentPost(mail));
			return;
		}

		activeChar.sendPacket(new ExShowSentPostList(activeChar));
	}
}