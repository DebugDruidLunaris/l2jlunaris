package ai.Zone.LairOfAntharas;

import gnu.trove.map.hash.TIntIntHashMap;
import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

public class BloodyKarik extends Fighter 
{
    private int BLOODYKARIK = 22854;
    private int BLOODYKARIK_COUNT = 5;
    private int BKARIK_D_M_CHANCE = 5;
    TIntIntHashMap spawned_minion = new TIntIntHashMap();

    public BloodyKarik(NpcInstance actor) 
	{
        super(actor);
        spawned_minion.put(1, 1);
    }

    @Override
    protected void onEvtDead(Creature killer) 
	{
        super.onEvtDead(killer);
        NpcInstance npc = getActor();
        if (Rnd.chance(BKARIK_D_M_CHANCE) && !spawned_minion.containsKey(npc.getObjectId())) 
		{
            for (int x = 0; x < BLOODYKARIK_COUNT; x++) 
			{
                NpcInstance mob = NpcHolder.getInstance().getTemplate(BLOODYKARIK).getNewInstance();
                mob.setSpawnedLoc(npc.getLoc());
                mob.setReflection(npc.getReflection());
                mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
                mob.spawnMe(mob.getSpawnedLoc());
                mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer.getPlayer(), 1);
                spawned_minion.put(mob.getObjectId(), 1);
            }
        }
        if (spawned_minion.containsKey(npc.getObjectId())) 
		{
            spawned_minion.remove(npc.getObjectId());
        }
    }
}