package handler.admincommands;

import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class AdminTeam extends ScriptAdminCommand
{
	enum Commands
	{
		admin_setteam
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		TeamType team = TeamType.NONE;
		if(wordList.length >= 2)
			for(TeamType t : TeamType.values())
				if(wordList[1].equalsIgnoreCase(t.name()))
					team = t;

		GameObject object = activeChar.getTarget();
		if(object == null || !object.isCreature())
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}

		((Creature) object).setTeam(team);
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}