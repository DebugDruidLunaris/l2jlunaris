package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.PetitionGroupHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.petition.PetitionMainGroup;
import jts.gameserver.network.serverpackets.ExResponseShowStepTwo;

public class RequestExShowStepTwo extends L2GameClientPacket
{
	private int _petitionGroupId;

	@Override
	protected void readImpl()
	{
		_petitionGroupId = readC();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_NEW_PETITION_SYSTEM)
			return;

		PetitionMainGroup group = PetitionGroupHolder.getInstance().getPetitionGroup(_petitionGroupId);
		if(group == null)
			return;

		player.setPetitionGroup(group);
		player.sendPacket(new ExResponseShowStepTwo(player, group));
	}
}