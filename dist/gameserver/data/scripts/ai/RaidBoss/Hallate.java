package ai.RaidBoss;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

/**
 * АИ для РБ Death Lord Hallate. Если z координаты меньше или
 * больше предназначеных, телепортируется обратно и ресает хп.
 */
public class Hallate extends Fighter {

    private static final int z1 = -2150;
    private static final int z2 = -1650;

    public Hallate(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        int z = actor.getZ();
        if (z > z2 || z < z1) {
            actor.teleToLocation(113548, 17061, -2125);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }
}
