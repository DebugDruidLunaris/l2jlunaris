package ai;

import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;

public class PrisonGuard extends Fighter
{
	private static final int RACE_STAMP = 10013;

	public PrisonGuard(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		// 18367 не агрятся
		NpcInstance actor = getActor();
		if(actor.isDead() || actor.getNpcId() == 18367)
			return false;

		if(target.getEffectList().getEffectsCountForSkill(Skill.SKILL_EVENT_TIMER) == 0)
			return false;

		return super.checkAggression(target);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;
		if(attacker.isSummon() || attacker.isPet())
			attacker = attacker.getPlayer();
		if(attacker.getEffectList().getEffectsCountForSkill(Skill.SKILL_EVENT_TIMER) == 0)
		{
			if(actor.getNpcId() == 18367)
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.PrisonGuard.1");
			else if(actor.getNpcId() == 18368)
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.PrisonGuard.2");

			Skill petrification = SkillTable.getInstance().getInfo(4578, 1); // Petrification
			actor.doCast(petrification, attacker, true);
			if(attacker.getPet() != null)
				actor.doCast(petrification, attacker.getPet(), true);

			return;
		}

		// 18367 не отвечают на атаку, но зовут друзей
		if(actor.getNpcId() == 18367)
		{
			notifyFriends(attacker, damage);
			return;
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(actor.getNpcId() == 18367 && killer.getPlayer().getEffectList().getEffectsBySkillId(Skill.SKILL_EVENT_TIMER) != null)
			Functions.addItem(killer.getPlayer(), RACE_STAMP, 1);

		super.onEvtDead(killer);
	}
}