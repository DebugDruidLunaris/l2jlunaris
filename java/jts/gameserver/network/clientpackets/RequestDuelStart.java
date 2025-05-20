package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.events.EventType;
import jts.gameserver.model.entity.events.impl.DuelEvent;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class RequestDuelStart extends L2GameClientPacket
{
	private String _name;
	private int _duelType;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_duelType = readD();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isProcessingRequest())
		{
			player.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(player.getOlympiadGame() != null || Olympiad.isRegistered(player))
		{
			player.sendActionFailed();
			player.sendPacket(new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addName(player));
			return;
		}

		Player target = World.getPlayer(_name);
		if(target == null || target == player)
		{
			player.sendPacket(SystemMsg.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}

		DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
		if(duelEvent == null)
			return;

		if(!duelEvent.canDuel(player, target, true))
			return;

		if(target.isBusy())
		{
			player.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return;
		}

		duelEvent.askDuel(player, target);
	}
}