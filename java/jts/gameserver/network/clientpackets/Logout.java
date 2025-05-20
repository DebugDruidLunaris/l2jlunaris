package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.utils.Log_New;

public class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl() {}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Dont allow leaving if player is fighting
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		// Prevent player from logging out if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(activeChar.isFestivalParticipant())
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
				activeChar.sendActionFailed();
				return;
			}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if (activeChar.isInStoreMode() && !activeChar.isInBuffStore() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}
        if (activeChar.isInAwayingMode())
        {
            activeChar.sendMessage(new CustomMessage("Away.ActionFailed", activeChar));
            activeChar.sendActionFailed();
            return;
        }
			activeChar.kick();

		if (activeChar.isInBuffStore())
			activeChar.offlineBuffStore();
		else
			activeChar.logout();
		Log_New.LogEvent(activeChar.getName(), "LogOut", "Logout", new String[] { "char: " + activeChar.getName() + " logged out from the game" });
	}
}