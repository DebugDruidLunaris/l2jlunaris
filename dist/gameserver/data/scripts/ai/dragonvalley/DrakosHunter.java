package ai.dragonvalley;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.NpcUtils;

public class DrakosHunter extends Fighter 
{
    public DrakosHunter(NpcInstance actor) 
	{
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
	{
        if (Rnd.chance(0.7D)) 
		{
            NpcInstance actor = getActor();
            for (int i = 0; i < 4; i++) 
			{
                NpcInstance n = NpcUtils.spawnSingle(22823, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
                n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
            }
        }
        super.onEvtAttacked(attacker, damage);
    }
}