package jts.gameserver.network.clientpackets;

import jts.commons.lang.ArrayUtils;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.RestartType;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.model.entity.residence.Castle;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.entity.residence.ResidenceFunction;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.ActionFail;
import jts.gameserver.network.serverpackets.Die;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.TeleportUtils;

import org.apache.commons.lang3.tuple.Pair;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(_restartType == null || activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}

		switch(_restartType)
		{
			case AGATHION:
				if(activeChar.isAgathionResAvailable())
					activeChar.doRevive(100);
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			case FIXED:
				if(activeChar.getPlayerAccess().ResurectFixed)
					activeChar.doRevive(100);
				else if(ItemFunctions.removeItem(activeChar, 13300, 1, true) == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else if(ItemFunctions.removeItem(activeChar, 10649, 1, true) == 1)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
			default:
				Location loc = null;
				Reflection ref = activeChar.getReflection();

				if(ref == ReflectionManager.DEFAULT)
					for(GlobalEvent e : activeChar.getEvents())
						loc = e.getRestartLoc(activeChar, _restartType);

				if(loc == null)
					loc = defaultLoc(_restartType, activeChar);

				if(loc != null)
				{
					Pair<Integer, OnAnswerListener> ask = activeChar.getAskListener(false);
					if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
						activeChar.getAskListener(true);

					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
				}
				else
					activeChar.sendPacket(ActionFail.STATIC, new Die(activeChar));
				break;
		}
	}

	//FIXME [VISTALL] вынести куда то?
	// телепорт к флагу, не обрабатывается, по дефалту
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();

		switch(restartType)
		{
			case TO_CLANHALL:
				if(clan != null && clan.getHasHideout() != 0)
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
					if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_CASTLE:
				if(clan != null && clan.getCastle() != 0)
				{
					Castle castle = activeChar.getCastle();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FORTRESS:
				if(clan != null && clan.getHasFortress() != 0)
				{
					Fortress fort = activeChar.getFortress();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
					if(fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_VILLAGE:
			default:
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
				break;
		}
		return loc;
	}
}