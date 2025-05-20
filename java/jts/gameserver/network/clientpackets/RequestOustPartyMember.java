package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.DimensionalRift;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class RequestOustPartyMember extends L2GameClientPacket
{
	//Format: cS
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Party party = activeChar.getParty();
		if(party == null || !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Вы не можете сейчас выйти из группы.");//TODO [G1ta0] custom message
			return;
		}

		Player member = party.getPlayerByName(_name);

		if(member == activeChar)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		Reflection r = party.getReflection();

		if(r != null && r instanceof DimensionalRift && member.getReflection().equals(r))
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.RequestOustPartyMember.CantOustInRift", activeChar));
		else if(r != null && !(r instanceof DimensionalRift))
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.RequestOustPartyMember.CantOustInDungeon", activeChar));
		else
			party.removePartyMember(member, true);
	}
}