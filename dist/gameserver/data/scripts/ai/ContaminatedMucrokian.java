package ai;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Fighter;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;

public class ContaminatedMucrokian extends Fighter 
{

    public ContaminatedMucrokian(NpcInstance actor) 
	{
        super(actor);
    }

    @Override
    protected void onIntentionAttack(Creature target) 
	{
        NpcInstance actor = getActor();
        if (actor == null) 
		{
            return;
        }
        if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) 
		{
            Functions.npcSay(actor, NpcString.NAIA_WAGANAGEL_PEUTAGUN, ChatType.ALL, 5000);
        }
        super.onIntentionAttack(target);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
	{
        NpcInstance actor = getActor();
        if (actor != null && !actor.isDead()) 
		{
            if (attacker != null) {
                if (attacker.getNpcId() >= 22656 && attacker.getNpcId() <= 22659) 
				{
                    if (Rnd.chance(100)) 
					{
                        actor.abortAttack(true, false);
                        actor.getAggroList().clear();
                        Location pos = Location.findPointToStay(actor, 450, 600);
                        if (GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), pos.x, pos.y, pos.z, actor.getGeoIndex())) 
						{
                            actor.setRunning();
                            addTaskMove(pos, false);
                        }
                    }
                }
            }
        }
        super.onEvtAttacked(attacker, damage);
    }
}