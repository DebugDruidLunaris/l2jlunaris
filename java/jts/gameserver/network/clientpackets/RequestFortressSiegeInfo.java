package jts.gameserver.network.clientpackets;

import java.util.List;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		List<Fortress> fortressList = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
		for(Fortress fort : fortressList)
			if(fort != null && fort.getSiegeEvent().isInProgress())
				activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
	}
}