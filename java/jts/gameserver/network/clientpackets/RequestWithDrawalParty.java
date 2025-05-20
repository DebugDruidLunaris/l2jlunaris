package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.DimensionalRift;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.utils.Log_New;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Вы не можете сейчас выйти из группы."); //TODO [G1ta0] custom message
			return;
		}

		Reflection r = activeChar.getParty().getReflection();
		if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.RequestWithDrawalParty.Rift", activeChar));
		else if(r != null && activeChar.isInCombat())
			activeChar.sendMessage("Вы не можете сейчас выйти из группы.");
		else
			activeChar.leaveParty();
		Log_New.LogEvent(activeChar.getName(), "Party", "LeftParty", new String[] { "" });
	}
}