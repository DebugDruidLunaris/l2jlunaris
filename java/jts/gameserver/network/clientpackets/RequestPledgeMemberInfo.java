package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public class RequestPledgeMemberInfo extends L2GameClientPacket
{
	// format: (ch)dS
	@SuppressWarnings("unused")
	private int _pledgeType;
	private String _target;

	@Override
	protected void readImpl()
	{
		_pledgeType = readD();
		_target = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Clan clan = activeChar.getClan();
		if(clan != null)
		{
			UnitMember cm = clan.getAnyMember(_target);
			if(cm != null)
				activeChar.sendPacket(new PledgeReceiveMemberInfo(cm));
		}
	}
}