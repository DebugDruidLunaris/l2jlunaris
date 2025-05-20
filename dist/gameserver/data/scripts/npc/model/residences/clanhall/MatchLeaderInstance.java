package npc.model.residences.clanhall;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class MatchLeaderInstance extends MatchBerserkerInstance
{
	public MatchLeaderInstance(int objectId, NpcTemplate template)
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