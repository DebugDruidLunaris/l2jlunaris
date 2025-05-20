package ai.RaidBoss;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

/**
 *  АИ для РБ Death Lord Ipos. Если z координаты меньше или больше
 * предназначеных, телепортируется обратно и ресает хп.
 */
public class Ipos extends Fighter
{

    private static final int x1 = 150000;
    private static final int x2 = 158000;
    private static final int y1 = -9740;

    public Ipos(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
    {
        NpcInstance actor = getActor();
        int x = actor.getX();
        int y = actor.getY();
        if (x < x1 || x > x2 || y < y1) 
        {
            actor.teleToLocation(154152, -13300, -3736);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }
}
