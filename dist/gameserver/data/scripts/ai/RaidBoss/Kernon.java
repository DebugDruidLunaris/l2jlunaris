package ai.RaidBoss;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

/**
 *  АИ для РБ Kernon. Если z координаты меньше или больше
 * предназначеных, телепортируется обратно и ресает хп.
 */
public class Kernon extends Fighter {

    private static final int z1 = 3900;
    private static final int z2 = 4300;

    public Kernon(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        int z = actor.getZ();
        if (z > z2 || z < z1) {
            actor.teleToLocation(113420, 16424, 3969);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }
}
