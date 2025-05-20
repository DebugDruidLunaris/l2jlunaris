package ai.dragonvalley;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.NpcUtils;

public class EmeraldDrake extends Mystic 
{
    public EmeraldDrake(NpcInstance actor) 
	{
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
	{
        if (Rnd.chance(0.7D)) 
		{
            NpcInstance actor = getActor();
            NpcInstance n = NpcUtils.spawnSingle(22860, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
            n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
        }
        super.onEvtAttacked(attacker, damage);
    }
}