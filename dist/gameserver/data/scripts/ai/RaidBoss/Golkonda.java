package ai.RaidBoss;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;

/**
 * АИ для РБ Golkonda. Если z координаты меньше или больше
 * предназначеных, телепортируется обратно и ресает хп.
 */
public class Golkonda extends Fighter {

    private static final int z1 = 6900;
    private static final int z2 = 7500;

    public Golkonda(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        int z = actor.getZ();
        if (z > z2 || z < z1) {
            actor.teleToLocation(116313, 15896, 6999);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }
}
