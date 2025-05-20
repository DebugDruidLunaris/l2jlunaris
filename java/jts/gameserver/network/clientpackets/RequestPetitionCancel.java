package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.instancemanager.PetitionManager;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.tables.GmListTable;

public final class RequestPetitionCancel extends L2GameClientPacket
{
	//private int _unknown;

	@Override
	protected void readImpl() {} //_unknown = readD(); This is pretty much a trigger packet.

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if(activeChar.isGM())
				PetitionManager.getInstance().endActivePetition(activeChar);
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.PETITION_UNDER_PROCESS));
		}
		else if(PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			if(PetitionManager.getInstance().cancelActivePetition(activeChar))
			{
				int numRemaining = Config.ALT_MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);

				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY).addString(String.valueOf(numRemaining)));

				// Notify all GMs that the player's pending petition has been cancelled.
				String msgContent = activeChar.getName() + " has canceled a pending petition.";
				GmListTable.broadcastToGMs(new Say2(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
			}
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER));
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_NOT_SUBMITTED_A_PETITION));
	}
}