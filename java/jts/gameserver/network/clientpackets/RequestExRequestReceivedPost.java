package jts.gameserver.network.clientpackets;

import jts.commons.dao.JdbcEntityState;
import jts.gameserver.dao.MailDAO;
import jts.gameserver.model.Player;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.network.serverpackets.ExChangePostState;
import jts.gameserver.network.serverpackets.ExReplyReceivedPost;
import jts.gameserver.network.serverpackets.ExShowReceivedPostList;

public class RequestExRequestReceivedPost extends L2GameClientPacket
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

		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			if(mail.isUnread())
			{
				mail.setUnread(false);
				mail.setJdbcState(JdbcEntityState.UPDATED);
				mail.update();
				activeChar.sendPacket(new ExChangePostState(true, Mail.READED, mail));
			}

			activeChar.sendPacket(new ExReplyReceivedPost(mail));
			return;
		}

		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
}