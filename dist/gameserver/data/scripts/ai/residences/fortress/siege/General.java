package ai.residences.fortress.siege;

import jts.commons.util.Rnd;
import jts.gameserver.model.Creature;
import jts.gameserver.model.entity.events.impl.FortressSiegeEvent;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.residence.Fortress;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import npc.model.residences.SiegeGuardInstance;
import ai.residences.SiegeGuardFighter;

public class General extends SiegeGuardFighter
{
	public General(NpcInstance actor)
	{
		super(actor);

		actor.addListener(FortressSiegeEvent.RESTORE_BARRACKS_LISTENER);
	}

	@Override
	public void onEvtAttacked(Creature attacker, int dam)
	{
		super.onEvtAttacked(attacker, dam);
		SiegeGuardInstance actor = getActor();

		if(Rnd.chance(1))
			Functions.npcSay(actor, NpcString.DO_YOU_NEED_MY_POWER_YOU_SEEM_TO_BE_STRUGGLING);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		SiegeGuardInstance actor = getActor();

		FortressSiegeEvent siegeEvent = actor.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
			return;

		if(siegeEvent.getResidence().getFacilityLevel(Fortress.GUARD_BUFF) > 0)
			actor.doCast(SkillTable.getInstance().getInfo(5432, siegeEvent.getResidence().getFacilityLevel(Fortress.GUARD_BUFF)), actor, false);

		siegeEvent.barrackAction(4, false);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		SiegeGuardInstance actor = getActor();
		FortressSiegeEvent siegeEvent = actor.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
			return;

		siegeEvent.barrackAction(4, true);
		siegeEvent.broadcastTo(SystemMsg.THE_BARRACKS_HAVE_BEEN_SEIZED, SiegeEvent.ATTACKERS, SiegeEvent.DEFENDERS);
		Functions.npcShout(actor, NpcString.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF);
		super.onEvtDead(killer);
		siegeEvent.checkBarracks();
	}
}