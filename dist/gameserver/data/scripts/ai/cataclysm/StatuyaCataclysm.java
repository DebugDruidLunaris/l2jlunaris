package ai.cataclysm;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class StatuyaCataclysm extends Fighter
{
	// Сдесь менять Ид хеалиров (по умолчанию гремлины)
	public int healer = 36802;

	public List<NpcInstance> npcs = new ArrayList<NpcInstance>();
	public boolean _attacked = false;

	public StatuyaCataclysm(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if( !_attacked)
		{
			NpcInstance actor = getActor();
			_attacked = true;
			for(int i = 0; i < 5; i++)
			{
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(healer));
				sp.setLoc(Location.findPointToStay(actor, 100, 120));
				NpcInstance npc = sp.doSpawn(true);
				HealerCataclysm ai = (HealerCataclysm) npc.getAI();
				ai.setMaster(actor);
				npcs.add(npc);
			}
		}
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}

	@Override
	protected void onEvtDead(Creature killer)
	{
		for(NpcInstance npc : npcs)
		{
			if(npc != null && !npc.isDead())
				npc.deleteMe();
		}
		_attacked = false;

		super.onEvtDead(killer);
	}
}