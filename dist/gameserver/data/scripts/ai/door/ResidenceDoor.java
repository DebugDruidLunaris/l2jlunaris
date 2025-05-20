package ai.door;

import jts.gameserver.ai.DoorAI;
import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.listener.actor.player.OnAnswerListener;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.instances.DoorInstance;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.ConfirmDlg;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class ResidenceDoor extends DoorAI
{
	public ResidenceDoor(DoorInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtTwiceClick(final Player player)
	{
		final DoorInstance door = getActor();

		Residence residence = ResidenceHolder.getInstance().getResidence(door.getTemplate().getAIParams().getInteger("residence_id"));
		if(residence.getOwner() != null && player.getClan() != null && player.getClan() == residence.getOwner() && (player.getClanPrivileges() & Clan.CP_CS_ENTRY_EXIT) == Clan.CP_CS_ENTRY_EXIT)
		{
			SystemMsg msg = door.isOpen() ? SystemMsg.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE : SystemMsg.WOULD_YOU_LIKE_TO_OPEN_THE_GATE;
			player.ask(new ConfirmDlg(msg, 0), new OnAnswerListener(){
				@Override
				public void sayYes()
				{
					if(door.isOpen())
						door.closeMe(player, true);
					else
						door.openMe(player, true);
				}

				@Override
				public void sayNo() {}
			});
		}
	}
}