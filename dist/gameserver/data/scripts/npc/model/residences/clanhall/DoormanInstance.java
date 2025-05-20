package npc.model.residences.clanhall;

import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class DoormanInstance extends npc.model.residences.DoormanInstance
{
	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getOpenPriv()
	{
		return Clan.CP_CH_ENTRY_EXIT;
	}

	@Override
	public Residence getResidence()
	{
		return getClanHall();
	}
}