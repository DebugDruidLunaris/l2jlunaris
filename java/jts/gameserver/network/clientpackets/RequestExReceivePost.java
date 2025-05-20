package jts.gameserver.network.clientpackets;

import java.util.Set;

import jts.commons.dao.JdbcEntityState;
import jts.commons.math.SafeMath;
import jts.gameserver.cache.Msg;
import jts.gameserver.dao.MailDAO;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.ItemInstance.ItemLocation;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.network.serverpackets.ExShowReceivedPostList;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

import org.apache.commons.lang3.StringUtils;

public class RequestExReceivePost extends L2GameClientPacket
{
	private int postId;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		postId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_EXCHANGE);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}

		Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
		if(mail != null)
		{
			activeChar.getInventory().writeLock();
			try
			{
				Set<ItemInstance> attachments = mail.getAttachments();
				ItemInstance[] items;

				if(attachments.size() > 0 && !activeChar.isInPeaceZone())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_IN_A_NON_PEACE_ZONE_LOCATION);
					return;
				}
				synchronized (attachments)
				{
					if(mail.getAttachments().isEmpty())
						return;

					items = mail.getAttachments().toArray(new ItemInstance[attachments.size()]);

					int slots = 0;
					long weight = 0;
					for(ItemInstance item : items)
					{
						weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));
						if(!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
							slots++;
					}

					if(!activeChar.getInventory().validateWeight(weight))
					{
						sendPacket(Msg.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
						return;
					}

					if(!activeChar.getInventory().validateCapacity(slots))
					{
						sendPacket(Msg.YOU_COULD_NOT_RECEIVE_BECAUSE_YOUR_INVENTORY_IS_FULL);
						return;
					}

					if(mail.getPrice() > 0)
					{
						if(!activeChar.reduceAdena(mail.getPrice(), true))
						{
							activeChar.sendPacket(Msg.YOU_CANNOT_RECEIVE_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
							return;
						}

						Player sender = World.getPlayer(mail.getSenderId());
						if(sender != null)
						{
							sender.addAdena(mail.getPrice(), true);
							sender.sendPacket(new SystemMessage(SystemMessage.S1_ACQUIRED_THE_ATTACHED_ITEM_TO_YOUR_MAIL).addName(activeChar));
						}
						else
						{
							int expireTime = 360 * 3600 + (int) (System.currentTimeMillis() / 1000L); //TODO [G1ta0] хардкод времени актуальности почты
							Mail reply = mail.reply();
							reply.setExpireTime(expireTime);

							ItemInstance item = ItemFunctions.createItem(ItemTemplate.ITEM_ID_ADENA);
							item.setOwnerId(reply.getReceiverId());
							item.setCount(mail.getPrice());
							item.setLocation(ItemLocation.MAIL);
							item.save();

							Log.LogItem(activeChar, Log.PostSend, item);

							reply.addAttachment(item);
							reply.save();
						}
					}

					attachments.clear();
				}

				mail.setJdbcState(JdbcEntityState.UPDATED);
				if(StringUtils.isEmpty(mail.getBody()))
					mail.delete();
				else
					mail.update();

				for(ItemInstance item : items)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ACQUIRED_S2_S1).addItemName(item.getItemId()).addNumber(item.getCount()));
					Log.LogItem(activeChar, Log.PostRecieve, item);
					Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "Mail", new String[] { "Mail recieved with item:", "item name: " + item.getName() + " count: " + item.getCount() + ", objId(" + item.getObjectId() + ") from charID " + mail.getSenderId() + "" });
					activeChar.getInventory().addItem(item);
				}

				activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_RECEIVED);
			}
			catch(ArithmeticException ae) {} //TODO audit
			finally
			{
				activeChar.getInventory().writeUnlock();
			}
		}

		activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
	}
}