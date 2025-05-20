package npc.model;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class HellboundRemnantInstance extends MonsterInstance
{
	public HellboundRemnantInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		super.reduceCurrentHp(Math.min(i, getCurrentHp() - 1), attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}

	public void onUseHolyWater(Creature user)
	{
		if(getCurrentHp() < 100)
			doDie(user);
	}
}