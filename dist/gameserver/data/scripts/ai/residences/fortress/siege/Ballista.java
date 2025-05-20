package ai.residences.fortress.siege;

import jts.commons.util.Rnd;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.entity.events.impl.FortressSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.instances.NpcInstance;

public class Ballista extends DefaultAI
{
	private static final int BALLISTA_BOMB_SKILL_ID = 2342;
	private int _bombsUseCounter;

	public Ballista(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(caster == null || skill.getId() != BALLISTA_BOMB_SKILL_ID)
			return;

		Player player = caster.getPlayer();
		FortressSiegeEvent siege = actor.getEvent(FortressSiegeEvent.class);
		FortressSiegeEvent siege2 = player.getEvent(FortressSiegeEvent.class);
		if(siege == null || siege != siege2 || siege.getSiegeClan(SiegeEvent.ATTACKERS, player.getClan()) == null)
			return;

		_bombsUseCounter++;
		if(Rnd.chance(20) || _bombsUseCounter > 4)
			actor.doDie(caster);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_bombsUseCounter = 0;
		super.onEvtDead(killer);
	}
}