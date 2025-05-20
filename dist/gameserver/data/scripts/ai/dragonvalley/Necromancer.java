package ai.dragonvalley;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Mystic;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.NpcUtils;

public class Necromancer extends Mystic 
{
    public Necromancer(NpcInstance actor) 
	{
        super(actor);
    }


    @Override
    protected void onEvtDead(Creature killer) 
	{
		super.onEvtDead(killer);
        if (Rnd.chance(30)) 
		{
            NpcInstance n = NpcUtils.spawnSingle(Rnd.chance(50) ? 22818 : 22819, getActor().getLoc());
            n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
        }
    }
}