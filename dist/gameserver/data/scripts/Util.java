import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.SevenSigns;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;

public class Util extends Functions
{
	private static final Logger _log = LoggerFactory.getLogger(Util.class);

	public void Gatekeeper(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		long price = Long.parseLong(param[param.length - 1]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(price > 0 && player.getAdena() < price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(player.getActiveWeaponFlagAttachment() != null)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}

		if(player.getMountType() == 2)
		{
			player.sendMessage("Teleportation riding a wyvern is impossible..");
			return;
		}

		/* Затычка, npc Mozella не ТПшит чаров уровень которых превышает заданный в конфиге
		 * Off Like >= 56 lvl, данные по ограничению lvl'a устанавливаются в altsettings.ini.
		 * PS  на офе нет не каких подобных затычек и ограничений
		 */
		if(player.getLastNpc() != null)
		{
			int npcId = player.getLastNpc().getNpcId();
			switch(npcId)
			{
				case 30483:
					if(player.getLevel() >= Config.CRUMA_GATEKEEPER_LVL)
					{
						show("teleporter/30483-no.htm", player);
						return;
					}
					break;
				case 32864:
				case 32865:
				case 32866:
				case 32867:
				case 32868:
				case 32869:
				case 32870:
					if((player.getKarma() > 0))
					{
						show("Smell like you try to run. You'r unlucky this time", player);
						return;
					}
					if(player.getLevel() < 80)
					{
						show("teleporter/" + npcId + "-no.htm", player);
						return;
					}
					break;
			}
		}
		if(player.isInCombat() && player.getPvpFlag() > 0)
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете телепортироваться в этом состоянии!" : "You cannot teleport in this state!");
			return;
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);
		int castleId = param.length > 4 ? Integer.parseInt(param[3]) : 0;

		if(player.getReflection().isDefault())
		{
			Castle castle = castleId > 0 ? ResidenceHolder.getInstance().getResidence(Castle.class, castleId) : null;
			// Нельзя телепортироваться в города, где идет осада
			if(castle != null && castle.getSiegeEvent().isInProgress())
			{
				player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
		}

		Location pos = Location.findPointToStay(x, y, z, 50, 100, player.getGeoIndex());

		if(price > 0)
			player.reduceAdena(price, true);
		player.teleToLocation(pos);
	}

	public void GatekeeperPcCafe(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		int price = Integer.parseInt(param[3]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(price > 0 && player.getPcBangPoints() < price)
		{
			player.sendPacket(Msg.YOU_ARE_SHORT_OF_ACCUMULATED_POINTS);
			return;
		}

		if(player.isTerritoryFlagEquipped() || player.isTerritoryFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}

		if(player.getMountType() == 2)
		{
			player.sendMessage(player.isLangRus() ? "Телепортация на лошадях или крылатом драконе невозможна." : "Teleportation riding horse, wyvern impossible.");
			return;
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);

		Location pos = Location.findPointToStay(x, y, z, 20, 70, player.getGeoIndex());

		player.teleToLocation(pos);

		if(price > 0)
			player.reducePcBangPoints(price);
		player.teleToLocation(pos);
	}

	public void SSGatekeeper(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		int type = Integer.parseInt(param[3]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(type > 0)
		{
			int player_cabal = SevenSigns.getInstance().getPlayerCabal(player);
			int period = SevenSigns.getInstance().getCurrentPeriod();
			int winner = SevenSigns.getInstance().getCabalHighestScore();

			if(period == SevenSigns.PERIOD_COMPETITION && player_cabal == SevenSigns.CABAL_NULL)
			{
				player.sendPacket(Msg.USED_ONLY_DURING_A_QUEST_EVENT_PERIOD);
				return;
			}

			if(period == SevenSigns.PERIOD_SEAL_VALIDATION && winner != SevenSigns.CABAL_NULL)
			{
				if(winner != player_cabal)
				{
					player.sendMessage("");
					return;
				}
				if(type == 1 && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE) != player_cabal)
				{
					player.sendMessage("");
					return;
				}
				if(type == 2 && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS) != player_cabal)
				{
					player.sendMessage("");
					return;
				}
			}
		}

		player.teleToLocation(Integer.parseInt(param[0]), Integer.parseInt(param[1]), Integer.parseInt(param[2]));
	}

	public void QuestGatekeeper(String[] param)
	{
		if(param.length < 5)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		long count = Long.parseLong(param[3]);
		int item = Integer.parseInt(param[4]);

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		if(count > 0)
		{
			if(!player.getInventory().destroyItemByItemId(item, count))
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return;
			}
			player.sendPacket(SystemMessage2.removeItems(item, count));
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);

		Location pos = Location.findPointToStay(x, y, z, 20, 70, player.getGeoIndex());

		player.teleToLocation(pos);
	}

	public void ReflectionGatekeeper(String[] param)
	{
		if(param.length < 5)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		player.setReflection(Integer.parseInt(param[4]));

		Gatekeeper(param);
	}

	/**
	 * Используется для телепортации за Newbie Token, проверяет уровень и передает
	 * параметры в QuestGatekeeper
	 */
	public void TokenJump(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(player.getLevel() <= 19)
			QuestGatekeeper(param);
		else
			show("Only for newbies", player);
	}

	public void NoblessTeleport()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(player.isNoble() || Config.ALT_ALLOW_NOBLE_TP_TO_ALL)
			show("scripts/noble.htm", player);
		else
			show("scripts/nobleteleporter-no.htm", player);
	}

	public void PayPage(String[] param)
	{
		if(param.length < 2)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		String page = param[0];
		int item = Integer.parseInt(param[1]);
		long price = Long.parseLong(param[2]);

		if(getItemCount(player, item) < price)
		{
			player.sendPacket(item == 57 ? Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA : SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		removeItem(player, item, price);
		show(page, player);
	}

/*	public void SimpleExchange(String[] param)
	{
		if(param.length < 4)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		int itemToTake = Integer.parseInt(param[0]);
		long countToTake = Long.parseLong(param[1]);
		int itemToGive = Integer.parseInt(param[2]);
		long countToGive = Long.parseLong(param[3]);

		if(getItemCount(player, itemToTake) < countToTake)
		{
			player.sendPacket(itemToTake == 57 ? Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA : SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}

		removeItem(player, itemToTake, countToTake);
		addItem(player, itemToGive, countToGive);
	}*/

	public void MakeEchoCrystal(String[] param)
	{
		if(param.length < 1)
			throw new IllegalArgumentException();

		Player player = getSelf();
		if(player == null)
			return;

		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;

		int crystal = Integer.parseInt(param[0]);

		if(getItemCount(player, 57) < 200)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		switch(crystal)
		{
			case 1:
			{
				if(getItemCount(player, 4410) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4411, 1);
					break;
				}
			}
			case 2:
			{
				if(getItemCount(player, 4409) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4412, 1);
					break;
				}
			}
			case 3:
			{
				if(getItemCount(player, 4408) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4413, 1);
					break;
				}
			}
			case 4:
			{
				if(getItemCount(player, 4420) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4414, 1);
					break;
				}
			}
			case 5:
			{
				if(getItemCount(player, 4421) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4415, 1);
					break;
				}
			}
			case 6:
			{
				if(getItemCount(player, 4419) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4417, 1);
					break;
				}
			}
			case 7:
			{
				if(getItemCount(player, 4418) == 0)
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 1");
					return;
				}
				else
				{
					player.getLastNpc().onBypassFeedback(player, "Chat 0");
					removeItem(player, 57, 200);
					addItem(player, 4416, 1);
					break;
				}
			}
			default:
			{
				player.getLastNpc().onBypassFeedback(player, "Chat 1");
				_log.warn("Player " + player.getName() + " tried to create item by ID: " + crystal + ".");
				_log.info("Cheater data: (Player: " + player.getName() + " Account: " + player.getAccountName() + " IP: " + player.getIP() + ").");
				return;
			}
		}
	}

	public void TakeNewbieWeaponCoupon()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.ALT_ALLOW_SHADOW_WEAPONS)
		{
			show(new CustomMessage("common.Disabled", player), player);
			return;
		}
		if(player.getLevel() > 19 || player.getClassId().getLevel() > 1)
		{
			show("Your level is too high!", player);
			return;
		}
		if(player.getLevel() < 6)
		{
			show("Your level is too low!", player);
			return;
		}
		if(player.getVarB("newbieweapon"))
		{
			show("Your already got your newbie weapon!", player);
			return;
		}
		addItem(player, 7832, 5);
		player.setVar("newbieweapon", "true", -1);
	}

	public void TakeAdventurersArmorCoupon()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.ALT_ALLOW_SHADOW_WEAPONS)
		{
			show(new CustomMessage("common.Disabled", player), player);
			return;
		}
		if(player.getLevel() > 39 || player.getClassId().getLevel() > 2)
		{
			show("Your level is too high!", player);
			return;
		}
		if(player.getLevel() < 20 || player.getClassId().getLevel() < 2)
		{
			show("Your level is too low!", player);
			return;
		}
		if(player.getVarB("newbiearmor"))
		{
			show("Your already got your newbie weapon!", player);
			return;
		}
		addItem(player, 7833, 1);
		player.setVar("newbiearmor", "true", -1);
	}

	public void enter_dc()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		player.setVar("DCBackCoords", player.getLoc().toXYZString(), -1);
		player.teleToLocation(-114582, -152635, -6742);
	}

	public void exit_dc()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(!NpcInstance.canBypassCheck(player, npc))
			return;

		String var = player.getVar("DCBackCoords");
		if(var == null || var.isEmpty())
		{
			player.teleToLocation(new Location(43768, -48232, -800), 0);
			return;
		}
		player.teleToLocation(Location.parseLoc(var), 0);
		player.unsetVar("DCBackCoords");
	}
}