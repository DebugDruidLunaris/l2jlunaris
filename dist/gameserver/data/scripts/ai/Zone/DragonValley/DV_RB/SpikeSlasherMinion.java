package ai.Zone.DragonValley.DV_RB;

import jts.commons.util.Rnd;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class SpikeSlasherMinion extends Fighter{
    
    private Skill paralysis = SkillTable.getInstance().getInfo(6878, 1);
    
    private long last_cast_anchor = 0;

    public SpikeSlasherMinion(NpcInstance actor) 
	{
        super(actor);
    }
    
    @Override
    protected void onEvtAttacked(Creature attacker, int damage) 
	{
        NpcInstance actor = getActor();
        if (last_cast_anchor < System.currentTimeMillis()) 
		{
            actor.doCast(paralysis, attacker, true);
            last_cast_anchor = System.currentTimeMillis() + Rnd.get(5,10) * 1000;
        }
        super.onEvtAttacked(attacker, damage);
    }
}