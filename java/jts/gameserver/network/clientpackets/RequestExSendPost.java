package jts.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jts.commons.dao.JdbcEntityState;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.captcha.CaptchaValidator;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.database.mysql;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.ItemInstance.ItemLocation;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.network.serverpackets.ExNoticePostArrived;
import jts.gameserver.network.serverpackets.ExReplyWritePost;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;
import jts.gameserver.utils.Util;

import org.apache.commons.lang3.ArrayUtils;

public class RequestExSendPost extends L2GameClientPacket
{
	private int _messageType;
	private String _recieverName, _topic, _body;
	private int _count;
	private int[] _items;
	private long[] _itemQ;
	private long _price;

	/**
	 * format: SdSS dx[dQ] Q
	 */
	@Override
	protected void readImpl()
	{
		_recieverName = readS(35); // имя адресата
		_messageType = readD(); // тип письма, 0 простое 1 с запросом оплаты
		_topic = readS(Byte.MAX_VALUE); // topic
		_body = readS(Short.MAX_VALUE); // body

		_count = readD(); // число прикрепленных вещей
		if(_count * 12 + 4 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1) //TODO [G1ta0] audit
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new long[_count];

		for(int i = 0; i < _count; i++)
		{
			_items[i] = readD(); // objectId
			_itemQ[i] = readQ(); // количество
			if(_itemQ[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				return;
			}
		}

		_price = readQ(); // цена для писем с запросом оплаты

		if(_price < 0)
		{
			_count = 0;
			_price = 0;
		}
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

		// Custom
		if(activeChar.isGM() && _recieverName.equalsIgnoreCase("ONLINE_ALL"))
		{
			Map<Integer, Long> map = new HashMap<Integer, Long>();
			if(_items != null && _items.length > 0)
				for(int i = 0; i < _items.length; i++)
				{
					ItemInstance item = activeChar.getInventory().getItemByObjectId(_items[i]);
					map.put(item.getItemId(), _itemQ[i]);
				}

			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				if(p != null && p.isOnline())
					Functions.sendSystemMail(p, _topic, _body, map);

			activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
			activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
			return;
		}

		if(!Config.ALLOW_MAIL)
		{
			activeChar.sendMessage(new CustomMessage("mail.Disabled", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getLevel() < Config.ALLOW_MAIL_LVL)
		{
			if (activeChar.isLangRus())
				activeChar.sendMessage("Доступно для персонажей " + Config.ALLOW_MAIL_LVL + " уровня или выше.");
			else 
				activeChar.sendMessage("Available for characters " + Config.ALLOW_MAIL_LVL + " level or higher.");
			return;
		}
		
		if ((System.currentTimeMillis() - getClient().getLastSendMail()) < 10000) // 10 sec
		{
			activeChar.sendPacket(SystemMsg.CANT_FORWARD_LESS_THAN_MINUTE);
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_EXCHANGE);
			return;
		}

		if(activeChar.getEnchantScroll() != null)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
			return;
		}

		if(activeChar.getName().equalsIgnoreCase(_recieverName))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_SEND_A_MAIL_TO_YOURSELF);
			return;
		}

		if(_count > 0 && !activeChar.isInPeaceZone())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_IN_A_NON_PEACE_ZONE_LOCATION);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(!activeChar.antiFlood.canMail())
		{
			if (activeChar.isLangRus())
				activeChar.sendMessage("Невозможно так часто отправлять почту");
			else 
				activeChar.sendMessage("Unable to send mail as often");
			return;
		}
		
		if(activeChar.isCaptchaChatBlocked())
			return;

		if(_price > 0)
		{
			if(!activeChar.getPlayerAccess().UseTrade)
			{
				activeChar.sendPacket(Msg.THIS_ACCOUNT_CANOT_TRADE_ITEMS);
				activeChar.sendActionFailed();
				return;
			}

			String tradeBan = activeChar.getVar("tradeBan");
			if(tradeBan != null && (tradeBan.equals("-1") || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
			{
				if(tradeBan.equals("-1"))
					activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently", activeChar));
				else
					activeChar.sendMessage(new CustomMessage("common.TradeBanned", activeChar).addString(Util.formatTime((int) (Long.parseLong(tradeBan) / 1000L - System.currentTimeMillis() / 1000L))));
				return;
			}
		}

		// ищем цель и проверяем блоклисты
		if(activeChar.isInBlockList(_recieverName)) // тем кто в блоклисте не шлем
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BLOCKED_C1).addString(_recieverName));
			return;
		}

		int recieverId;
		Player target = World.getPlayer(_recieverName);
		if(target != null)
		{
			recieverId = target.getObjectId();
			_recieverName = target.getName();
			if(target.isInBlockList(activeChar)) // цель заблокировала отправителя
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_).addString(_recieverName));
				return;
			}
		}
		else
		{
			recieverId = CharacterDAO.getInstance().getObjectIdByName(_recieverName);
			if(recieverId > 0)
				//TODO [G1ta0] корректировать _recieverName
				if(mysql.simple_get_int("target_Id", "character_blocklist", "obj_Id=" + recieverId + " AND target_Id=" + activeChar.getObjectId()) > 0) // цель заблокировала отправителя
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_).addString(_recieverName));
					return;
				}
		}

		if(recieverId == 0) // не нашли цель?
		{
			activeChar.sendPacket(Msg.WHEN_THE_RECIPIENT_DOESN_T_EXIST_OR_THE_CHARACTER_HAS_BEEN_DELETED_SENDING_MAIL_IS_NOT_POSSIBLE);
			return;
		}

		int expireTime = (_messageType == 1 ? 12 : 360) * 3600 + (int) (System.currentTimeMillis() / 1000L); //TODO [G1ta0] хардкод времени актуальности почты

		if(_count > 8) //клиент не дает отправить больше 8 вещей
		{
			activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		long serviceCost = 100 + _count * 1000; //TODO [G1ta0] хардкод цена за почту

		List<ItemInstance> attachments = new ArrayList<ItemInstance>();

		activeChar.getInventory().writeLock();
		try
		{
			if(activeChar.getAdena() < serviceCost)
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
				return;
			}

			// подготовить аттачи
			if(_count > 0)
				for(int i = 0; i < _count; i++)
				{
					ItemInstance item = activeChar.getInventory().getItemByObjectId(_items[i]);
					if(item == null || item.getCount() < _itemQ[i] || item.getItemId() == ItemTemplate.ITEM_ID_ADENA && item.getCount() < _itemQ[i] + serviceCost || !item.canBeTraded(activeChar))
					{
						activeChar.sendPacket(Msg.THE_ITEM_THAT_YOU_RE_TRYING_TO_SEND_CANNOT_BE_FORWARDED_BECAUSE_IT_ISN_T_PROPER);
						return;
					}
				}

			if(!activeChar.reduceAdena(serviceCost, true))
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_FORWARD_BECAUSE_YOU_DON_T_HAVE_ENOUGH_ADENA);
				return;
			}

			if(_count > 0)
			{
				for(int i = 0; i < _count; i++)
				{
					ItemInstance item = activeChar.getInventory().removeItemByObjectId(_items[i], _itemQ[i]);

					Log.LogItem(activeChar, Log.PostSend, item);
					Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "Mail", new String[] { "Mail sent with item:", "item name: " + item.getName() + " count: " + item.getCount() + ", objId(" + item.getObjectId() + ")" });
					item.setOwnerId(activeChar.getObjectId());
					item.setLocation(ItemLocation.MAIL);
					if(item.getJdbcState().isSavable())
					{
						item.save();
					}
					else
					{
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();
					}

					attachments.add(item);
				}
			}
		}
		finally
		{
			activeChar.getInventory().writeUnlock();
		}

		Mail mail = new Mail();
		mail.setSenderId(activeChar.getObjectId());
		mail.setSenderName(activeChar.getName());
		mail.setReceiverId(recieverId);
		mail.setReceiverName(_recieverName);
		mail.setTopic(_topic);
		mail.setBody(_body);
		mail.setPrice(_messageType > 0 ? _price : 0);
		mail.setUnread(true);
		mail.setType(Mail.SenderType.NORMAL);
		mail.setExpireTime(expireTime);
		for(ItemInstance item : attachments)
			mail.addAttachment(item);
		mail.save();

		activeChar.sendPacket(ExReplyWritePost.STATIC_TRUE);
		activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_SENT);
		MailCaptchaReqest();

		if(target != null)
		{
			target.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			target.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
		}
	}
	
		private void MailCaptchaReqest()
		{
			if(Config.CAPTCHA_ENABLE)
			{
				Player activeChar = getClient().getActiveChar();
				if(Rnd.get(0, 100) < 10 && activeChar.getCaptchaReguest() < System.currentTimeMillis())
				{
					CaptchaValidator.getInstance().sendCaptcha(activeChar);
					activeChar.setCaptchaReguest();
				}
			}
		}
	}