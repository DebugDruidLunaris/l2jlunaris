package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.Request;
import jts.gameserver.model.Request.L2RequestType;
import jts.gameserver.model.World;
import jts.gameserver.network.serverpackets.AskJoinParty;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.IStaticPacket;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.AutoHuntingPunish;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_itemDistribution = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		Player target = World.getPlayer(_name);
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}

		if(target == activeChar)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			activeChar.sendActionFailed();
			return;
		}

		if(target.isBusy())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return;
		}
		if(Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			if(target.isBeingPunished())
			{
				if(target.getPlayerPunish().canJoinParty() && target.getBotPunishType() == AutoHuntingPunish.Punish.PARTYBAN)
					target.endPunishment();
				else if(target.getBotPunishType() == AutoHuntingPunish.Punish.PARTYBAN)
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_PARTICIPATING_IN_A_PARTY_IS_NOT_ALLOWED);
					return;
				}
			}
			if(activeChar.isBeingPunished())
			{
				if(activeChar.getPlayerPunish().canJoinParty())
					activeChar.endPunishment();
				else if(activeChar.getBotPunishType() == AutoHuntingPunish.Punish.PARTYBAN)
				{
					SystemMsg msg;
					switch(activeChar.getPlayerPunish().getDuration())
					{
						case 3600:
							msg = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_60_MINUTES;
							break;
						case 7200:
							msg = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_120_MINUTES;
							break;
						case 10800:
							msg = SystemMsg.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_PARTY_PARTICIPATION_WILL_BE_BLOCKED_FOR_180_MINUTES;
							break;
						default:
							msg = SystemMsg.THAT_IS_AN_INCORRECT_TARGET;
							break;
					}
					activeChar.sendPacket(msg);
					return;
				}
			}
		}
		IStaticPacket problem = target.canJoinParty(activeChar);
		if(problem != null)
		{
			activeChar.sendPacket(problem);
			return;
		}

		if(activeChar.isInParty())
		{
			if(activeChar.getParty().getMemberCount() >= Party.MAX_SIZE)
			{
				activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
				return;
			}

			// Только Party Leader может приглашать новых членов
			if(Config.ALT_PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
			{
				activeChar.sendPacket(SystemMsg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
				return;
			}

			if(activeChar.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.RequestJoinParty.InDimensionalRift", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}

		new Request(L2RequestType.PARTY, activeChar, target).setTimeout(10000L).set("itemDistribution", _itemDistribution);

		target.sendPacket(new AskJoinParty(activeChar.getName(), _itemDistribution));
		activeChar.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_BEEN_INVITED_TO_THE_PARTY).addName(target));
	}
}