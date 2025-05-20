package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.CharMoveToLocation;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.AutoHuntingPunish;
import jts.gameserver.utils.Location;

// cdddddd(d)
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private Location _targetLoc = new Location();
	private Location _originLoc = new Location();
	private int _moveMovement;

	/**
	 * packet type id 0x0f
	 */
	@Override
	protected void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		if(_buf.hasRemaining())
			_moveMovement = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		if(System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setLastMovePacket();
		// Bot punishment restriction
				if(Config.ENABLE_AUTO_HUNTING_REPORT && activeChar.isPlayer())
				{
					if(activeChar.isBeingPunished())
					{
						if(activeChar.getPlayerPunish().canWalk()  && activeChar.getPlayerPunish().getBotPunishType() == AutoHuntingPunish.Punish.MOVEBAN)
							activeChar.endPunishment();
						else if(activeChar.getPlayerPunish().getBotPunishType() == AutoHuntingPunish.Punish.MOVEBAN)
						{
							activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_MOVEMENT_IS_PROHIBITED_FOR_120_MINUTES);
							return;
						}
					}
				}
		if(activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFrozen())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
			return;
		}

		if(activeChar.isInObserverMode())
		{
			if(activeChar.getOlympiadObserveGame() == null)
				activeChar.sendActionFailed();
			else
				activeChar.sendPacket(new CharMoveToLocation(activeChar.getObjectId(), _originLoc, _targetLoc));
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTeleMode() > 0)
		{
			if(activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			activeChar.sendActionFailed();
			activeChar.teleToLocation(_targetLoc);
			return;
		}

		if(activeChar.isInFlyingTransform())
			_targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z)); // В летающей трансформе нельзя летать ниже, чем 0, и выше, чем 6000

		activeChar.moveToLocation(_targetLoc, 0, _moveMovement != 0 && !activeChar.getVarB("no_pf"));
	}
}