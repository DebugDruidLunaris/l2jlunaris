package ai.crypts_of_disgrace;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class ContaminatedBaturCommander extends Fighter
{
	private static final int TurkaCommanderChief = 22707; // Turka Commander in Chief
	private static final int CHANCE = 10; // Шанс спауна Turka Commander in Chief и миньенов

	public ContaminatedBaturCommander(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(CHANCE)) // Если повезло
		{
			// Спауним
			NpcInstance actor = getActor();
			SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(TurkaCommanderChief));
			sp.setLoc(Location.findPointToStay(actor, 100, 120));
			NpcInstance npc = sp.doSpawn(true);

			// Натравливаем на атакующего
			if(killer.isPet() || killer.isSummon())
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(2, 100));
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer.getPlayer(), Rnd.get(1, 100));
		}

		super.onEvtDead(killer);
	}
}