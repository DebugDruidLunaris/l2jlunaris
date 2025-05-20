package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.utils.Location;

public class MoSMonk extends Fighter
{
	public MoSMonk(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		@SuppressWarnings("unused")
		NpcInstance actor = getActor();
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && Rnd.chance(20))
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.MoSMonk.onIntentionAttack");
		super.onIntentionAttack(target);
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		if(target.getActiveWeaponInstance() == null)
			return false;
		return super.checkAggression(target);
	}
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;
			
		if(Rnd.chance(1))
		{
			if(Rnd.chance(10))
				spawnGumiel(actor);
		}

		super.onEvtDead(killer);
	}

	protected void spawnGumiel(NpcInstance actor)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(32759).getNewInstance();
			Location pos = Location.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
			npc.setSpawnedLoc(pos);
			npc.setReflection(actor.getReflection());
			npc.spawnMe(npc.getSpawnedLoc());
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.MoSMonk.Gumiel");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}