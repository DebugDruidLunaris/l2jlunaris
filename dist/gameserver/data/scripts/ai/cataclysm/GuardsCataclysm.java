package ai.cataclysm;

import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;

public class GuardsCataclysm extends Fighter
{
	// Максимальное число отдаления от точки спавна
	private final int pursuitRange = 100;

	// ТУТ НАСТРОИТЬ 9 ИД мирных статуй
	private final int statuya1 = 36805;
	private final int statuya2 = 36805;
	private final int statuya3 = 36805;
	private final int statuya4 = 36805;
	private final int statuya5 = 36805;
	private final int statuya6 = 36805;
	private final int statuya7 = 36805;
	private final int statuya8 = 36805;
	private final int statuya9 = 36805;

	private NpcInstance mob;

	public GuardsCataclysm(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = pursuitRange;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor != null)
		{
			if(mob != null && !mob.isDead())
			{
				actor.stopMove();
				actor.setRunning();
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, mob);
				mob.getAggroList().addDamageHate(actor, 10, 200);
			}
			else if(mob != null && mob.isDead())
			{
				if(actor.isRunning())
					actor.setWalking();

				if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
					setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

				mob = null;
			}

			if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && mob == null)
			{
				for(NpcInstance npc : World.getAroundNpc(actor, 500, 500))
				{
					if(npc.getNpcId() == statuya1 || npc.getNpcId() == statuya2 || npc.getNpcId() == statuya3 || npc.getNpcId() == statuya4 || npc.getNpcId() == statuya5 || npc.getNpcId() == statuya6 || npc.getNpcId() == statuya7 || npc.getNpcId() == statuya8 || npc.getNpcId() == statuya9)
						mob = npc;
				}
			}
		}
		return super.thinkActive();
	}
}