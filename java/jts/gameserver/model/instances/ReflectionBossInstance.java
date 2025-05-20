package jts.gameserver.model.instances;

import jts.gameserver.model.Creature;
import jts.gameserver.templates.npc.NpcTemplate;

@SuppressWarnings("serial")
public class ReflectionBossInstance extends RaidBossInstance
{
	private final static int COLLAPSE_AFTER_DEATH_TIME = 5; // 5 мин

	public ReflectionBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		getMinionList().unspawnMinions();
		super.onDeath(killer);
		clearReflection();
	}

	/**
	 * Удаляет все спауны из рефлекшена и запускает 5ти минутный коллапс-таймер.
	 */
	protected void clearReflection()
	{
		getReflection().clearReflection(COLLAPSE_AFTER_DEATH_TIME, true);
	}
}