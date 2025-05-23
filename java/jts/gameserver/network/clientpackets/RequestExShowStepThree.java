package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.model.Player;
import jts.gameserver.model.petition.PetitionMainGroup;
import jts.gameserver.model.petition.PetitionSubGroup;
import jts.gameserver.network.serverpackets.ExResponseShowContents;

public class RequestExShowStepThree extends L2GameClientPacket
{
	private int _subId;

	@Override
	protected void readImpl()
	{
		_subId = readC();
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_NEW_PETITION_SYSTEM)
			return;

		PetitionMainGroup group = player.getPetitionGroup();
		if(group == null)
			return;

		PetitionSubGroup subGroup = group.getSubGroup(_subId);
		if(subGroup == null)
			return;

		player.sendPacket(new ExResponseShowContents(subGroup.getDescription(player.getLanguage())));
	}
}