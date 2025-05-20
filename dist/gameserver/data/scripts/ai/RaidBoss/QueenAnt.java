package ai.RaidBoss;

import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

/**
 * АИ для РБ QueenAnt. Если z координаты меньше или больше
 * предназначеных, телепортируется обратно и ресает хп.
 */
public class QueenAnt extends Mystic
{

    private static final int z1 = -5000;
    private static final int z2 = -7500;

    public QueenAnt(NpcInstance actor) 
    {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
    {
        NpcInstance actor = getActor();
        int z = actor.getZ();
        if (z < z2 || z > z1) 
        {
            actor.teleToLocation(-21610, 181594, -5734);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }
}
