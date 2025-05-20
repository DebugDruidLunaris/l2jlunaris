package ai.RaidBoss;

import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SkillList;

/**
 * АИ для РБ Barakiel. Если координаты x и y меньше или
 * больше предназначеных, телепортируется обратно и ресает хп.
 * @modification : Prototype
 */
public class Barakiel extends Fighter
{

    private static final int x1 = 89920;
    private static final int x2 = 92124;
    private static final int y1 = -86872;

    public Barakiel(NpcInstance actor)
    {
        super(actor);
    }

    protected void onEvtSpawn()
    {
        if (Config.ALLOW_ANNOUNCE_NOBLE_RB)
        {
            Announcements.getInstance().announceToAll("Нублесс РБ Barakiel появился в мире!");
        }
        super.onEvtSpawn();
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
    {
        NpcInstance actor = getActor();
        int x = actor.getX();
        int y = actor.getY();
        if (x < x1 || x > x2 || y < y1) 
        {
            actor.teleToLocation(91008, -85904, -2736);
            actor.setCurrentHp(actor.getMaxHp(), false);
        }
        super.onEvtAttacked(attacker, damage);
    }

    protected void onEvtDead(Creature killer)
    {
        Player player = killer.getPlayer();
        if (Config.KILL_BARAKIEL_SET_NOBLE)
        {
            if (player.getParty() != null )
            {
                for (Player pc : player.getParty().getPartyMembers())
                {
                    if (!pc.isNoble())
                    {
                        Olympiad.addNoble(pc);
                        pc.setNoble(true);
                        pc.updatePledgeClass();
                        pc.updateNobleSkills();
                        pc.sendPacket(new SkillList(pc));
                        pc.broadcastUserInfo(true);
                    }
                }
            } 
            else if (!player.isNoble())
            {
                Olympiad.addNoble(player);
                player.setNoble(true);
                player.updatePledgeClass();
                player.updateNobleSkills();
                player.sendPacket(new SkillList(player));
                player.broadcastUserInfo(true);
            }
        }
        if (Config.ALLOW_ANNOUNCE_NOBLE_RB)
        {
            Announcements.getInstance().announceToAll("Нублесс РБ Barakiel был повержен!");
        }
        super.onEvtDead(killer);
    }
}