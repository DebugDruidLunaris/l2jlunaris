package npc.model.residences.castle;

import java.util.List;
import java.util.Set;

import jts.gameserver.model.Creature;
import jts.gameserver.model.entity.events.impl.CastleSiegeEvent;
import jts.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import jts.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class CastleFlameTowerInstance extends SiegeToggleNpcInstance
{
	private Set<String> _zoneList;

	public CastleFlameTowerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onDeathImpl(Creature killer)
	{
		CastleSiegeEvent event = getEvent(CastleSiegeEvent.class);
		if(event == null || !event.isInProgress())
			return;

		for(String s : _zoneList)
		{
			List<CastleDamageZoneObject> objects = event.getObjects(s);
			for(CastleDamageZoneObject zone : objects)
				zone.getZone().setActive(false);
		}
	}

	@Override
	public void setZoneList(Set<String> set)
	{
		_zoneList = set;
	}
}