package jts.gameserver.network.clientpackets;

import java.util.Calendar;
import java.util.List;

import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.dao.ClanListServiceDAO;
import jts.gameserver.dao.MailDAO;
import jts.gameserver.data.StringHolder;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.database.OfflineBuffersTable;
import jts.gameserver.instancemanager.CoupleManager;
import jts.gameserver.instancemanager.CursedWeaponsManager;
import jts.gameserver.instancemanager.PetitionManager;
import jts.gameserver.instancemanager.PlayerMessageStack;
import jts.gameserver.instancemanager.QuestManager;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import jts.gameserver.model.ClanListObject;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Effect;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Summon;
import jts.gameserver.model.World;
import jts.gameserver.model.GameObjectTasks.UnJailTask;
import jts.gameserver.model.base.InvisibleType;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.mail.Mail;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.SubUnit;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.network.GameClient;
import jts.gameserver.network.serverpackets.ChangeWaitType;
import jts.gameserver.network.serverpackets.ClientSetTime;
import jts.gameserver.network.serverpackets.ConfirmDlg;
import jts.gameserver.network.serverpackets.Die;
import jts.gameserver.network.serverpackets.EtcStatusUpdate;
import jts.gameserver.network.serverpackets.ExAutoSoulShot;
import jts.gameserver.network.serverpackets.ExBR_PremiumState;
import jts.gameserver.network.serverpackets.ExBasicActionList;
import jts.gameserver.network.serverpackets.ExGoodsInventoryChangedNotify;
import jts.gameserver.network.serverpackets.ExMPCCOpen;
import jts.gameserver.network.serverpackets.ExNoticePostArrived;
import jts.gameserver.network.serverpackets.ExNotifyPremiumItem;
import jts.gameserver.network.serverpackets.ExPCCafePointInfo;
import jts.gameserver.network.serverpackets.ExReceiveShowPostFriend;
import jts.gameserver.network.serverpackets.ExSetCompassZoneCode;
import jts.gameserver.network.serverpackets.ExShowScreenMessage;
import jts.gameserver.network.serverpackets.ExStorageMaxCount;
import jts.gameserver.network.serverpackets.HennaInfo;
import jts.gameserver.network.serverpackets.L2FriendList;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MagicSkillLaunched;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.PartySmallWindowAll;
import jts.gameserver.network.serverpackets.PartySpelled;
import jts.gameserver.network.serverpackets.PetInfo;
import jts.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import jts.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import jts.gameserver.network.serverpackets.PledgeSkillList;
import jts.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import jts.gameserver.network.serverpackets.PrivateStoreMsgSell;
import jts.gameserver.network.serverpackets.QuestList;
import jts.gameserver.network.serverpackets.RecipeShopMsg;
import jts.gameserver.network.serverpackets.RelationChanged;
import jts.gameserver.network.serverpackets.Ride;
import jts.gameserver.network.serverpackets.SSQInfo;
import jts.gameserver.network.serverpackets.ShortCutInit;
import jts.gameserver.network.serverpackets.SkillCoolTime;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.skills.AbnormalEffect;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.AdminFunctions;
import jts.gameserver.utils.GameStats;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.Log_New;
import jts.gameserver.utils.TradeHelper;
import GameGuard.GameGuard;
import GameGuard.network.GuardManager;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
	private static final Object _lock = new Object();

	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	protected void readImpl()
	{
		//readS(); - клиент всегда отправляет строку "narcasse"
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}

		int MyObjectId = activeChar.getObjectId();
		Long MyStoreId = activeChar.getStoredId();

		synchronized (_lock)//TODO [G1ta0] че это за хуйня, и почему она тут
		{
			for(Player cha : GameObjectsStorage.getAllPlayersForIterate())
			{
				if(MyStoreId == cha.getStoredId())
					continue;
				try
				{
					if(cha.getObjectId() == MyObjectId)
					{
						_log.warn("Double EnterWorld for char: " + activeChar.getName());
						cha.kick();
					}
				}
				catch(Exception e)
				{
					_log.error("", e);
				}
			}
		}

		GameStats.incrementPlayerEnterGame();

		boolean first = activeChar.entering;

		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN)
				activeChar.setInvisibleType(InvisibleType.NORMAL);

			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.spawnMe();

			if(activeChar.isInStoreMode() && activeChar.isInBuffStore())
			{
				if(!TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType()))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
			}

			// Synerge - If its in a buff store, remove it on login
			else if(activeChar.isInBuffStore())
			{
				activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				activeChar.broadcastCharInfo();
			}

			activeChar.setRunning();
			activeChar.standUp();
			activeChar.startTimers();
			Log_New.LogEvent(activeChar.getName(), "EnterGame", "EnterGame", new String[] { "char: " + activeChar.getName() + " entered into game" });
		}

		if(activeChar.isInvul())
			activeChar.setIsInvul(false);

		if(!activeChar.isHero() && !activeChar.isFakeHero())
		{
			for(ItemInstance item : activeChar.getInventory().getItems())
			{
				if(item.isHeroWeapon())
				{
					activeChar.getInventory().destroyItemByItemId(item.getItemId(), 1L);
				}
			}
		}

		if(!activeChar.isHero() && !activeChar.FakeHeroSkill())
		{
			Hero.removeSkills(activeChar);
		}

		if(GameGuard.isProtectionOn())//
		{
			GuardManager.SendSpecialSting(client);
		}
		checkNewMail(activeChar);
		activeChar.sendPacket(new ExBR_PremiumState(activeChar, activeChar.hasBonus()));
		if(Config.SERVICES_CLAN_ACADEM_ENABLED)
		{
			boolean needBroadcastinfo = false;
			
			if(activeChar.isClanLeader())
			{
				List<ClanListObject> listClan = ClanListServiceDAO.getInstance().getData(3);
				for(ClanListObject object : listClan)
					if(activeChar.getClanId() == object.get_obj_id())
					{
						activeChar.setLectureMark(2);
						needBroadcastinfo = true;
						break;
					}
				
				listClan.clear();
				
				List<ClanListObject> listAcadem = ClanListServiceDAO.getInstance().getData(2);
				for(ClanListObject object : listAcadem)
					if(activeChar.getClanId() == object.get_obj_id())
					{
						activeChar.setLectureMark(2);
						needBroadcastinfo = true;
						break;
					}
				
				listAcadem.clear();
			}
			else if(activeChar.getClanId() == 0)
			{
				List<ClanListObject> listCandidate = ClanListServiceDAO.getInstance().getData(1);
				for(ClanListObject object : listCandidate)
					if(activeChar.getObjectId() == object.get_obj_id())
					{
						activeChar.setLectureMark(1);
						needBroadcastinfo = true;
						break;
					}
				
				listCandidate.clear();
			}
			
			if(needBroadcastinfo)
				activeChar.broadcastUserInfo(true);
		}
		if(!activeChar.hasBonus())
		{
			activeChar.stopBonusTask();
		}

		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
		activeChar.sendItemList(false);
		activeChar.sendPacket(new ShortCutInit(activeChar), new SkillList(activeChar), new SkillCoolTime(activeChar));
		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

		Announcements.getInstance().showAnnouncements(activeChar);

		if(first)
			activeChar.getListeners().onEnter();

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

		if(first && activeChar.getCreateTime() > 0)
		{
			Calendar create = Calendar.getInstance();
			create.setTimeInMillis(activeChar.getCreateTime());
			Calendar now = Calendar.getInstance();

			int day = create.get(Calendar.DAY_OF_MONTH);
			if(create.get(Calendar.MONTH) == Calendar.FEBRUARY && day == 29)
				day = 28;

			int myBirthdayReceiveYear = activeChar.getVarInt(Player.MY_BIRTHDAY_RECEIVE_YEAR, 0);
			if(create.get(Calendar.MONTH) == now.get(Calendar.MONTH) && create.get(Calendar.DAY_OF_MONTH) == day)
				if(myBirthdayReceiveYear == 0 && create.get(Calendar.YEAR) != now.get(Calendar.YEAR) || myBirthdayReceiveYear > 0 && myBirthdayReceiveYear != now.get(Calendar.YEAR))
				{
					Mail mail = new Mail();
					mail.setSenderId(1);
					mail.setSenderName(StringHolder.getInstance().getNotNull(activeChar, "birthday.npc"));
					mail.setReceiverId(activeChar.getObjectId());
					mail.setReceiverName(activeChar.getName());
					mail.setTopic(StringHolder.getInstance().getNotNull(activeChar, "birthday.title"));
					mail.setBody(StringHolder.getInstance().getNotNull(activeChar, "birthday.text"));

					ItemInstance item = ItemFunctions.createItem(21169);
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(1L);
					item.save();

					mail.addAttachment(item);
					mail.setUnread(true);
					mail.setType(Mail.SenderType.BIRTHDAY);
					mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					mail.save();

					activeChar.setVar(Player.MY_BIRTHDAY_RECEIVE_YEAR, String.valueOf(now.get(Calendar.YEAR)), -1);
				}
		}

		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);

			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}

		// engage and notify Partner
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills();
		}

		sendPacket(new L2FriendList(activeChar), new ExStorageMaxCount(activeChar), new QuestList(activeChar), new ExBasicActionList(activeChar), new EtcStatusUpdate(activeChar));

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();

		if(Config.ENTER_WORLD_SHOW_HTML_WELCOME)
		{
			if(activeChar.getClan() != null && activeChar.getClan().isNoticeEnabled()&& activeChar.getClan().getNotice() != "")
			{
			}
			else
			{
			activeChar.sendPacket(new NpcHtmlMessage(5).setFile("welcome.htm").replace("<?servername?>", Config.SERVER_NAME));
			}
		}
		if(Config.ENTER_WORLD_SHOW_HTML_PREMIUM_BUY)
		{
			if(activeChar.getClan() != null && activeChar.getClan().isNoticeEnabled()&& activeChar.getClan().getNotice() != "")
			{
			}
			else
			if(activeChar.getNetConnection().getBonus() <= 1)
			activeChar.sendPacket(new NpcHtmlMessage(5).setFile("advertise.htm").replace("<?servername?>", Config.SERVER_NAME));
			}

		if(Config.ENTER_WORLD_ANNOUNCEMENTS_HERO_LOGIN)
		{
			if(activeChar.isHero() || activeChar.isFakeHero())
			{
				String[] param = { String.valueOf(activeChar.getName()) };
				DifferentMethods.sayToAll("jts.gameserver.network.clientpackets.EnterWorld.Hero", param);
			}
		}

		if(Config.ENTER_WORLD_ANNOUNCEMENTS_LORD_LOGIN)
		{
			if(activeChar.getClan() != null && activeChar.isClanLeader() && activeChar.getClan().getCastle() != 0)
			{
				int id = activeChar.getCastle().getId();

				String[] param = { String.valueOf(activeChar.getName()), String.valueOf(new CustomMessage("common.castle." + id + "", activeChar)) };
				DifferentMethods.sayToAll("jts.gameserver.network.clientpackets.EnterWorld.Lord", param);
			}
		}

		if (Config.ENT_SHOWENTERMESSON)
	        {
			activeChar.sendPacket(new ExShowScreenMessage(Config.ENT_SHOWENTERMESS, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, first));
	        }
		
			if(activeChar.isGM())
			{
				activeChar.sendMessage("Версия Сервера: .......... " + Config.SERVER_VERSION);
				activeChar.sendMessage("Дата Ревизии: ........... " + Config.SERVER_BUILD_DATE);
				activeChar.sendMessage("Сайт Команды: ....... Prototype-Project.ru ");
				activeChar.sendMessage("Информация Указана только Админ персонажам ");
			}
		if(Config.ALT_PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			if(activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				Skill castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if(castingSkill != null && castingTarget != null && castingTarget.isCreature() && activeChar.getAnimationEndTime() > 0)
					sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
			}

			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));

			if(activeChar.isMoving || activeChar.isFollow)
				sendPacket(activeChar.movePacket());

			if(activeChar.getMountNpcId() != 0)
				sendPacket(new Ride(activeChar));

			if(activeChar.isFishing())
				activeChar.stopFishing();
		}

		activeChar.entering = false;
		activeChar.sendUserInfo(true);

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitType(activeChar, ChangeWaitType.WT_SITTING));
		if(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			if(activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
				sendPacket(new PrivateStoreMsgBuy(activeChar));
			else if(activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_SELL || activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
				sendPacket(new PrivateStoreMsgSell(activeChar));
			else if(activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopMsg(activeChar));

		if(activeChar.isDead())
			sendPacket(new Die(activeChar));

		activeChar.unsetVar("offline");
		activeChar.unsetVar("offline_buff");

		//за попытку бега из тюрьмы садим назад выполняя все действия 
		if (activeChar.getVar("jailed") != null)	
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы в тюрьме: Вам осталось " + activeChar.getVar("jailed") + " минут(ы).");
			else
				activeChar.sendMessage("You are in jail " + activeChar.getVar("jailed") + " minutes left.");
			activeChar.teleToLocation(Location.findPointToStay(activeChar, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
			activeChar.sitDown(null);
			activeChar.block();
			activeChar._unjailTask = ThreadPoolManager.getInstance().schedule(new UnJailTask(activeChar), Integer.parseInt(activeChar.getVar("jailed")) * 60000);
		}
		// на всякий случай
		activeChar.sendActionFailed();

		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			//silence
			if(activeChar.getVarB("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			//invul
			if(activeChar.getVarB("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			//gmspeed
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
			}
			catch(Exception E)
			{}
		}

		if(!activeChar.isGM())
			activeChar.setIsInvul(false);

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		sendPacket(ClientSetTime.STATIC, new ExSetCompassZoneCode(activeChar));

		Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
			sendPacket(new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));

		if(activeChar.isCursedWeaponEquipped())
		{
			// Обновляем скилл и статы игрока, который владеет проклятым оружием.
			CursedWeaponsManager cursedManagerInstance = CursedWeaponsManager.getInstance();
			cursedManagerInstance.getCursedWeapon(activeChar.getCursedWeaponEquippedId()).giveSkillAndUpdateStats();
			cursedManagerInstance.showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		}
		if(first)
		{
			// Synerge - Si loguea y el pj estaba en un buff store, debemos quitarselo
			if(Config.BUFF_STORE_ENABLED)
				OfflineBuffersTable.getInstance().onLogin(activeChar);
		}
		if(!first)
		{
			//Персонаж вылетел во время просмотра
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
					activeChar.returnFromObserverMode();
				else if(activeChar.getOlympiadObserveGame() != null)
					activeChar.leaveOlympiadObserverMode(true);
				else if(activeChar.getOlympiadObserveGame() != null)
					activeChar.leaveOlympiadObserverMode(true);
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar);

			if(activeChar.getPet() != null)
				sendPacket(new PetInfo(activeChar.getPet()));

			if(activeChar.isInParty())
			{
				Summon member_pet;
				//sends new member party window for all members
				//we do all actions before adding member to a list, this speeds things up a little
				sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));

				for(Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
						sendPacket(new PartySpelled(member, true));
						if((member_pet = member.getPet()) != null)
							sendPacket(new PartySpelled(member_pet, true));

						sendPacket(RelationChanged.update(activeChar, member, activeChar));
					}

				// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
				if(activeChar.getParty().isInCommandChannel())
					sendPacket(ExMPCCOpen.STATIC);
			}

			for(int shotId : activeChar.getAutoSoulShot())
				sendPacket(new ExAutoSoulShot(shotId, true));

			for(Effect e : activeChar.getEffectList().getAllFirstEffects())
				if(e.getSkill().isToggle())
					sendPacket(new MagicSkillLaunched(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar));

			activeChar.broadcastCharInfo();
		}
		else
			activeChar.sendUserInfo(); // Отобразит права в клане

		activeChar.updateEffectIcons();
		activeChar.updateStats();

		if(Functions.IsActive("PcCafePointsExchange"))
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));

		if(Functions.IsActive("PcCafePointsExchange") && activeChar.getVar("pcBangOnStart") == null)
		{
			activeChar.addPcBangPoints(Config.ALT_PCBANG_POINTS_ON_START, false);
			activeChar.setVar("pcBangOnStart", String.valueOf(Config.ALT_PCBANG_POINTS_ON_START), System.currentTimeMillis() / 1000L + 86400);
		}
		else if(Functions.IsActive("PcCafePointsExchange"))
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы уже получали очки PC Cafe сегодня" : "You already received PC Cafe points today");

		if(!activeChar.getPremiumItemList().isEmpty())
			activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);

		activeChar.sendVoteSystemInfo();
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
		activeChar.getNevitSystem().onEnterWorld();
	}

	private static void notifyClanMembers(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		SubUnit subUnit = activeChar.getSubUnit();
		if(clan == null || subUnit == null)
			return;

		UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
		if(member == null)
			return;

		member.setPlayerInstance(activeChar, false);

		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		L2GameServerPacket msg = new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
		PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for(Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
			else if(clanMember.getObjectId() == apprentice)
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
			else
				clanMember.sendPacket(msg);
		}

		if(!activeChar.isClanLeader())
			return;

		ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
			return;

		if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
			return;

		if(clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < clanHall.getRentalFee())
			activeChar.sendPacket(new SystemMessage2(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
	}

	private void loadTutorial(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), "UC", null);
	}

	private void checkNewMail(Player activeChar)
	{
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
			if(mail.isUnread())
			{
				sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
	}
}