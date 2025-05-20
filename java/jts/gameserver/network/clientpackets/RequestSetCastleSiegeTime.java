package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.CastleSiegeInfo;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _id, _time;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_time = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _id);
		if(castle == null)
			return;

		if(player.getClan().getCastle() != castle.getId())
			return;

		if((player.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) != Clan.CP_CS_MANAGE_SIEGE)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
			return;
		}

		CastleSiegeEvent siegeEvent = castle.getSiegeEvent();

		siegeEvent.setNextSiegeTime(_time);

		player.sendPacket(new CastleSiegeInfo(castle, player));
	}
}