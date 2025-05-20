package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.pledge.Alliance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.tables.ClanTable;
import jts.gameserver.utils.Log_New;

public class RequestDismissAlly extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		Alliance alliance = clan.getAlliance();
		if(alliance == null)
		{
			activeChar.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return;
		}

		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
			return;
		}

		if(alliance.getMembersCount() > 1)
		{
			activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
			return;
		}

		ClanTable.getInstance().dissolveAlly(activeChar);
		Log_New.LogEvent(activeChar.getName(), "Alliance", "DissolveAlly", new String[] { "ally dissolved" });
	}
}