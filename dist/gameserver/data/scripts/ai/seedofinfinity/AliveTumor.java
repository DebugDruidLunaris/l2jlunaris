package ai.seedofinfinity;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

import org.apache.commons.lang3.ArrayUtils;

public class AliveTumor extends DefaultAI
{
	private long checkTimer = 0;
	private int coffinsCount = 0;
	private static final int[] regenCoffins = { 18706, 18709, 18710 };

	public AliveTumor(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();

		if(checkTimer + 10000 < System.currentTimeMillis())
		{
			checkTimer = System.currentTimeMillis();
			int i = 0;
			for(NpcInstance n : actor.getAroundNpc(400, 300))
				if(ArrayUtils.contains(regenCoffins, n.getNpcId()) && !n.isDead())
					i++;
			if(coffinsCount != i)
			{
				coffinsCount = i;
				coffinsCount = Math.min(coffinsCount, 12);
				if(coffinsCount > 0)
					actor.altOnMagicUseTimer(actor, SkillTable.getInstance().getInfo(5940, coffinsCount));
			}
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature target, int aggro) {}
}