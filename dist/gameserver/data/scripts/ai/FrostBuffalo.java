package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class FrostBuffalo extends Fighter
{
	private boolean _mobsNotSpawned = true;
	private static final int MOBS = 22093;
	private static final int MOBS_COUNT = 4;

	public FrostBuffalo(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(skill.isMagic())
			return;
		if(_mobsNotSpawned)
		{
			_mobsNotSpawned = false;
			for(int i = 0; i < MOBS_COUNT; i++)
				try
				{
					SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(MOBS));
					sp.setLoc(Location.findPointToStay(actor, 100, 120));
					NpcInstance npc = sp.doSpawn(true);
					if(caster.isPet() || caster.isSummon())
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster, Rnd.get(2, 100));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster.getPlayer(), Rnd.get(1, 100));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_mobsNotSpawned = true;
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return _mobsNotSpawned;
	}
}