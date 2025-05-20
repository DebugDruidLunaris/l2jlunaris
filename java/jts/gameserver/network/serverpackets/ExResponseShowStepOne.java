package jts.gameserver.network.serverpackets;

import java.util.Collection;

import jts.gameserver.data.xml.holder.PetitionGroupHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.petition.PetitionMainGroup;
import jts.gameserver.utils.Language;

public class ExResponseShowStepOne extends L2GameServerPacket
{
	private Language _language;

	public ExResponseShowStepOne(Player player)
	{
		_language = player.getLanguage();
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xAE);
		Collection<PetitionMainGroup> petitionGroups = PetitionGroupHolder.getInstance().getPetitionGroups();
		writeD(petitionGroups.size());
		for(PetitionMainGroup group : petitionGroups)
		{
			writeC(group.getId());
			writeS(group.getName(_language));
		}
	}
}