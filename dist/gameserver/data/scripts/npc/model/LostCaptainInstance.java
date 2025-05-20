package npc.model;

import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.Creature;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.ReflectionBossInstance;
import jts.gameserver.templates.InstantZone;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.Location;

@SuppressWarnings("serial")
public class LostCaptainInstance extends ReflectionBossInstance
{
	private static final int TELE_DEVICE_ID = 4314;

	public LostCaptainInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		Reflection r = getReflection();
		r.setReenterTime(System.currentTimeMillis());

		super.onDeath(killer);

		InstantZone iz = r.getInstancedZone();
		if(iz != null)
		{
			String tele_device_loc = iz.getAddParams().getString("tele_device_loc", null);
			if(tele_device_loc != null)
			{
				KamalokaGuardInstance npc = new KamalokaGuardInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(TELE_DEVICE_ID));
				npc.setSpawnedLoc(Location.parseLoc(tele_device_loc));
				npc.setReflection(r);
				npc.spawnMe(npc.getSpawnedLoc());
			}
		}
	}
}