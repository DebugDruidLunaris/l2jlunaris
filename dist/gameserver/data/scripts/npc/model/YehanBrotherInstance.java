package npc.model;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.RaidBossInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class YehanBrotherInstance extends RaidBossInstance
{
	public YehanBrotherInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp)
	{
		if(getBrother().getCurrentHp() > 500 && damage > getCurrentHp())
			damage = getCurrentHp() - 1;
		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if(!getBrother().isDead())
			getBrother().doDie(killer);
	}

	private NpcInstance getBrother()
	{
		int brotherId = 0;
		if(getNpcId() == 25665)
			brotherId = 25666;
		else if(getNpcId() == 25666)
			brotherId = 25665;
		for(NpcInstance npc : getReflection().getNpcs())
			if(npc.getNpcId() == brotherId)
				return npc;
		return null;
	}
}