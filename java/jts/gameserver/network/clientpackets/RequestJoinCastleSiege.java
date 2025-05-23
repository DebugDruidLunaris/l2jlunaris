package jts.gameserver.network.clientpackets;

import jts.gameserver.dao.SiegeClanDAO;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.SiegeClanObject;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.entity.residence.ResidenceType;
import jts.gameserver.model.pledge.Alliance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.Privilege;
import jts.gameserver.network.serverpackets.CastleSiegeAttackerList;
import jts.gameserver.network.serverpackets.CastleSiegeDefenderList;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class RequestJoinCastleSiege extends L2GameClientPacket
{
	private int _id;
	private boolean _isAttacker;
	private boolean _isJoining;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_isAttacker = readD() == 1;
		_isJoining = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR))
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		Residence residence = ResidenceHolder.getInstance().getResidence(_id);

		if(residence.getType() == ResidenceType.Castle)
			registerAtCastle(player, (Castle) residence, _isAttacker, _isJoining);
		else if(residence.getType() == ResidenceType.ClanHall && _isAttacker)
			registerAtClanHall(player, (ClanHall) residence, _isJoining);
	}

	private static void registerAtCastle(Player player, Castle castle, boolean attacker, boolean join)
	{
		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();

		Clan playerClan = player.getClan();

		SiegeClanObject siegeClan = null;
		if(attacker)
			siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, playerClan);
		else
		{
			siegeClan = siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, playerClan);
			if(siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, playerClan);
		}

		if(join)
		{
			Residence registeredCastle = null;
			for(Residence residence : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				SiegeClanObject tempCastle = residence.getSiegeEvent().getSiegeClan(SiegeEvent.ATTACKERS, playerClan);

				if(tempCastle == null)
					tempCastle = residence.getSiegeEvent().getSiegeClan(SiegeEvent.DEFENDERS, playerClan);

				if(tempCastle == null)
					tempCastle = residence.getSiegeEvent().getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, playerClan);

				if(tempCastle != null)
					registeredCastle = residence;
			}

			if(attacker)
			{
				if(castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}

				Alliance alliance = playerClan.getAlliance();
				if(alliance != null)
					for(Clan clan : alliance.getMembers())
						if(clan.getCastle() == castle.getId())
						{
							player.sendPacket(SystemMsg.YOU_CANNOT_REGISTER_AS_AN_ATTACKER_BECAUSE_YOU_ARE_IN_AN_ALLIANCE_WITH_THE_CASTLE_OWNING_CLAN);
							return;
						}
				if(playerClan.getCastle() > 0)
				{
					player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}

				if(siegeClan != null)
				{
					player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}

				if(playerClan.getLevel() < 5)
				{
					player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}

				if(registeredCastle != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
					return;
				}

				if(siegeEvent.isRegistrationOver())
				{
					player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}

				if(castle.getSiegeDate().getTimeInMillis() == 0)
				{
					player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}

				int allSize = siegeEvent.getObjects(SiegeEvent.ATTACKERS).size();
				if(allSize >= CastleSiegeEvent.MAX_SIEGE_CLANS)
				{
					player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
					return;
				}

				Fortress fortress = player.getFortress();
				if(fortress != null && fortress.getCastleId() == castle.getId())
				{
					player.sendPacket(SystemMsg.SIEGE_REGISTRATION_IS_NOT_POSSIBLE_DUE_TO_YOUR_CASTLE_CONTRACT);
					return;
				}

				siegeClan = new SiegeClanObject(SiegeEvent.ATTACKERS, playerClan, 0);
				siegeEvent.addObject(SiegeEvent.ATTACKERS, siegeClan);

				SiegeClanDAO.getInstance().insert(castle, siegeClan);

				player.sendPacket(new CastleSiegeAttackerList(castle));
			}
			else
			{
				if(castle.getOwnerId() == 0)
					return;

				if(castle.getOwnerId() == playerClan.getClanId())
				{
					player.sendPacket(SystemMsg.CASTLE_OWNING_CLANS_ARE_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE);
					return;
				}

				if(playerClan.getCastle() > 0)
				{
					player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE);
					return;
				}

				if(siegeClan != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
					return;
				}

				if(playerClan.getLevel() < 5)
				{
					player.sendPacket(SystemMsg.ONLY_CLANS_OF_LEVEL_5_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE);
					return;
				}

				if(registeredCastle != null)
				{
					player.sendPacket(SystemMsg.YOU_HAVE_ALREADY_REQUESTED_A_CASTLE_SIEGE);
					return;
				}

				if(castle.getSiegeDate().getTimeInMillis() == 0)
				{
					player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE);
					return;
				}

				if(siegeEvent.isRegistrationOver())
				{
					player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
					return;
				}

				siegeClan = new SiegeClanObject(CastleSiegeEvent.DEFENDERS_WAITING, playerClan, 0);
				siegeEvent.addObject(CastleSiegeEvent.DEFENDERS_WAITING, siegeClan);

				SiegeClanDAO.getInstance().insert(castle, siegeClan);

				player.sendPacket(new CastleSiegeDefenderList(castle));
			}
		}
		else
		{
			if(siegeClan == null)
				siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_REFUSED, playerClan);

			if(siegeClan == null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}

			if(siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}

			siegeEvent.removeObject(siegeClan.getType(), siegeClan);

			SiegeClanDAO.getInstance().delete(castle, siegeClan);
			if(siegeClan.getType() == SiegeEvent.ATTACKERS)
				player.sendPacket(new CastleSiegeAttackerList(castle));
			else
				player.sendPacket(new CastleSiegeDefenderList(castle));
		}
	}

	private static void registerAtClanHall(Player player, ClanHall clanHall, boolean join)
	{
		ClanHallSiegeEvent siegeEvent = clanHall.getSiegeEvent();

		Clan playerClan = player.getClan();

		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, playerClan);

		if(join)
		{
			if(playerClan.getHasHideout() > 0)
			{
				player.sendPacket(SystemMsg.A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE);
				return;
			}

			if(siegeClan != null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST);
				return;
			}

			if(playerClan.getLevel() < 4)
			{
				player.sendPacket(SystemMsg.ONLY_CLANS_WHO_ARE_LEVEL_4_OR_ABOVE_CAN_REGISTER_FOR_BATTLE_AT_DEVASTATED_CASTLE_AND_FORTRESS_OF_THE_DEAD);
				return;
			}

			if(siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}

			int allSize = siegeEvent.getObjects(SiegeEvent.ATTACKERS).size();
			if(allSize >= CastleSiegeEvent.MAX_SIEGE_CLANS)
			{
				player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE);
				return;
			}

			siegeClan = new SiegeClanObject(SiegeEvent.ATTACKERS, playerClan, 0);
			siegeEvent.addObject(SiegeEvent.ATTACKERS, siegeClan);

			SiegeClanDAO.getInstance().insert(clanHall, siegeClan);
		}
		else
		{
			if(siegeClan == null)
			{
				player.sendPacket(SystemMsg.YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE);
				return;
			}

			if(siegeEvent.isRegistrationOver())
			{
				player.sendPacket(SystemMsg.YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER);
				return;
			}

			siegeEvent.removeObject(siegeClan.getType(), siegeClan);

			SiegeClanDAO.getInstance().delete(clanHall, siegeClan);
		}

		player.sendPacket(new CastleSiegeAttackerList(clanHall));
	}
}