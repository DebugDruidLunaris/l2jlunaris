package ai;

import java.util.Collection;
import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;

public class EvilSpiritsMagicForce extends Fighter 
{

    private NpcInstance mob = null;
    private boolean _firstTimeAttacked = true;
    public static final NpcString[] MsgText = 
	{
        NpcString.AH_AH_FROM_THE_MAGIC_FORCE_NO_MORE_I_WILL_BE_FREED,
        NpcString.EVEN_THE_MAGIC_FORCE_BINDS_YOU_YOU_WILL_NEVER_BE_FORGIVEN};

    public EvilSpiritsMagicForce(NpcInstance actor) 
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
        super.onIntentionAttack(target);
    }

    @Override
    protected boolean thinkActive() 
	{
        NpcInstance actor = getActor();
        if (actor == null || actor.isDead()) 
		{
            return true;
        }

        if (mob == null) 
		{
            Collection<NpcInstance> around = getActor().getAroundNpc(300, 300);
            if (around != null && !around.isEmpty()) 
			{
                for (NpcInstance npc : around) 
				{
                    if (npc.getNpcId() >= 22650 && npc.getNpcId() <= 22655) 
					{
                        if (mob == null || getActor().getDistance3D(npc) < getActor().getDistance3D(mob)) 
						{
                            mob = npc;
                        }
                    }
                }
            }

        }
        if (mob != null) 
		{
            actor.stopMove();
            actor.setRunning();
            getActor().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, mob, 1);
            return true;
        }
        return false;
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
	{
        NpcInstance actor = getActor();
        if (actor == null) 
		{
            return;
        }

        if (_firstTimeAttacked) 
		{
            _firstTimeAttacked = false;
            if (Rnd.chance(5)) 
			{
                Functions.npcSay(actor, Rnd.get(MsgText), ChatType.ALL, 5000);
            }
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(Creature killer) 
	{
        _firstTimeAttacked = true;
        super.onEvtDead(killer);
    }
}