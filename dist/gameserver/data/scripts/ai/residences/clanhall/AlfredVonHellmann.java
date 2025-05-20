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
import ai.residences.SiegeGuardFighter;

public class AlfredVonHellmann extends SiegeGuardFighter
{
	public static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(5000, 1);
	public static final Skill DRAIN_SKILL = SkillTable.getInstance().getInfo(5001, 1);

	private static Zone ZONE_3 = ReflectionUtils.getZone("lidia_zone3");

	public AlfredVonHellmann(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		ZONE_3.setActive(true);
		Functions.npcShout(getActor(), NpcString.HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGAN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		super.onEvtDead(killer);

		ZONE_3.setActive(false);

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
			addTaskCast(attacker, DRAIN_SKILL);

		Creature target = actor.getAggroList().getMostHated();
		if(target == attacker && Rnd.chance(0.3))
			addTaskCast(attacker, DAMAGE_SKILL);
	}
}