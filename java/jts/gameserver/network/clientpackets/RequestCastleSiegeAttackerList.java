package jts.gameserver.network.clientpackets;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.network.serverpackets.CastleSiegeAttackerList;

public class RequestCastleSiegeAttackerList extends L2GameClientPacket
{
	private int _unitId;

	@Override
	protected void readImpl()
	{
		_unitId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Residence residence = ResidenceHolder.getInstance().getResidence(_unitId);
		if(residence != null)
			sendPacket(new CastleSiegeAttackerList(residence));
	}
}