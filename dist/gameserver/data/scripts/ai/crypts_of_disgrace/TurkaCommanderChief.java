package ai.crypts_of_disgrace;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;

public class TurkaCommanderChief extends Fighter
{
	private static final int TurkaCommanderMinion = 22706; // Миньен
	private static final int MinionCount = 2; // Количество миньенов
	private static final int Guardian = 18815; // Guardian of the Burial Grounds
	private static final int CHANCE = 10;
	
	public TurkaCommanderChief(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		for(int i = 0; i < MinionCount; i++) // При спауне главного спауним и миньенов
			npcSpawn(TurkaCommanderMinion);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(Rnd.chance(CHANCE)) // Если повезло
		{
			// Спауним гварда
			NpcInstance npc = npcSpawn(Guardian);

			// И натравливаем его
			if(killer.isPet() || killer.isSummon())
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(2, 100));
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer.getPlayer(), Rnd.get(1, 100));
		}

		super.onEvtDead(killer);
	}
	
	private NpcInstance npcSpawn(int template)
	{
		NpcInstance actor = getActor();
		SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(template));
		sp.setLoc(Location.findPointToStay(actor, 100, 120));
		return sp.doSpawn(true);
	}
}