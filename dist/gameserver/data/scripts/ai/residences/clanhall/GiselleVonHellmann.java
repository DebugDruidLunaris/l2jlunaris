package ai.residences.clanhall;

import jts.commons.util.Rnd;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Zone;
import jts.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import jts.gameserver.model.entity.events.objects.SpawnExObject;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.ReflectionUtils;
import ai.residences.SiegeGuardMystic;

public class GiselleVonHellmann extends SiegeGuardMystic
{
	private static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(5003, 1);

	private static final Zone ZONE_1 = ReflectionUtils.getZone("lidia_zone1");
	private static final Zone ZONE_2 = ReflectionUtils.getZone("lidia_zone2");

	public GiselleVonHellmann(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();

		ZONE_1.setActive(true);
		ZONE_2.setActive(true);

		Functions.npcShout(getActor(), NpcString.ARISE_MY_FAITHFUL_SERVANTS_YOU_MY_PEOPLE_WHO_HAVE_INHERITED_THE_BLOOD);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		super.onEvtDead(killer);

		ZONE_1.setActive(false);
		ZONE_2.setActive(false);

		Functions.npcShout(actor, NpcString.AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL);

		ClanHallSiegeEvent siegeEvent = actor.getEvent(ClanHallSiegeEvent.class);
		if(siegeEvent == null)
			return;
		SpawnExObject spawnExObject = siegeEvent.getFirstObject(ClanHallSiegeEvent.BOSS);
		NpcInstance lidiaNpc = spawnExObject.getFirstSpawned();

		if(lidiaNpc.getCurrentHpRatio() == 1.)
			lidiaNpc.setCurrentHp(lidiaNpc.getMaxHp() / 2, true);
	}

	@Override
	public void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();

		super.onEvtAttacked(attacker, damage);

		if(PositionUtils.calculateDistance(attacker, actor, false) > 300. && Rnd.chance(0.13))
			addTaskCast(attacker, DAMAGE_SKILL);
	}
}