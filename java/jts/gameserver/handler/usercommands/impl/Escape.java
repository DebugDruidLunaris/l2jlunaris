package jts.gameserver.handler.usercommands.impl;

import jts.gameserver.cache.Msg;
import jts.gameserver.handler.usercommands.IUserCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.tables.SkillTable;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 52 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(activeChar.isMovementDisabled() || activeChar.isOutOfControl() || activeChar.isInOlympiadMode())
			return false;

		if(activeChar.getTeleMode() != 0 || !activeChar.getPlayerAccess().UseTeleport)
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar));
			return false;
		}

		if(activeChar.isTerritoryFlagEquipped())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}

		if(activeChar.isInDuel() || activeChar.getTeam() != TeamType.NONE)
		{
			activeChar.sendMessage(new CustomMessage("common.RecallInDuel", activeChar));
			return false;
		}

		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.stopMove();

		Skill skill;
		if(activeChar.getPlayerAccess().FastUnstuck)
			skill = SkillTable.getInstance().getInfo(1050, 2);
		else
			skill = SkillTable.getInstance().getInfo(2099, 1);

		if(skill != null && skill.checkCondition(activeChar, activeChar, false, false, true))
			activeChar.getAI().Cast(skill, activeChar, false, true);

		return true;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}