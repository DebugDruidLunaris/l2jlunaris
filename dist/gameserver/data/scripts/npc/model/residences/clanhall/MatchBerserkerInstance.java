package npc.model.residences.clanhall;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.residences.clanhall.CTBBossInstance;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class MatchBerserkerInstance extends CTBBossInstance
{
	public MatchBerserkerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if(attacker.isPlayer())
			damage = damage / getMaxHp() / 0.05 * 100;
		else
			damage = damage / getMaxHp() / 0.05 * 10;

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
}