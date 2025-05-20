package ai.dragonvalley;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class ExplodingOrcGhost extends Fighter
{

    private Skill SELF_DESTRUCTION = SkillTable.getInstance().getInfo(6850, 1);

    public ExplodingOrcGhost(NpcInstance actor) 
	{
        super(actor);
    }

    @Override
    protected void onEvtSpawn() 
	{
        ThreadPoolManager.getInstance().schedule(new StartSelfDestructionTimer(getActor()), 3000L);
        super.onEvtSpawn();
    }

    private class StartSelfDestructionTimer extends RunnableImpl 
	{
        private NpcInstance _npc;

        public StartSelfDestructionTimer(NpcInstance npc) 
		{
            _npc = npc;
        }

        @Override
        public void runImpl() 
		{
            _npc.abortAttack(true, false);
            _npc.abortCast(true, false);
            _npc.doCast(SELF_DESTRUCTION, _actor, true);
        }
    }
}