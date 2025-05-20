package jts.gameserver.network.clientpackets;

import jts.gameserver.dao.SiegeClanDAO;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.events.objects.SiegeClanObject;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.CastleSiegeDefenderList;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class RequestConfirmCastleSiegeWaitingList extends L2GameClientPacket
{
	private boolean _approved;
	private int _unitId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
		_clanId = readD();
		_approved = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getClan() == null)
			return;

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);

		if(castle == null || player.getClan().getCastle() != castle.getId())
		{
			player.sendActionFailed();
			return;
		}

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();

		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(CastleSiegeEvent.DEFENDERS_WAITING, _clanId);
		if(siegeClan == null)
			siegeClan = siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, _clanId);

		if(siegeClan == null)
			return;

		if((player.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) != Clan.CP_CS_MANAGE_SIEGE)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
			return;
		}

		if(siegeEvent.isRegistrationOver())
		{
			player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
			return;
		}

		int allSize = siegeEvent.getObjects(SiegeEvent.DEFENDERS).size();
		if(allSize >= CastleSiegeEvent.MAX_SIEGE_CLANS)
		{
			player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
			return;
		}

		siegeEvent.removeObject(siegeClan.getType(), siegeClan);

		if(_approved)
			siegeClan.setType(SiegeEvent.DEFENDERS);
		else
			siegeClan.setType(CastleSiegeEvent.DEFENDERS_REFUSED);

		siegeEvent.addObject(siegeClan.getType(), siegeClan);

		SiegeClanDAO.getInstance().update(castle, siegeClan);

		player.sendPacket(new CastleSiegeDefenderList(castle));
	}
}