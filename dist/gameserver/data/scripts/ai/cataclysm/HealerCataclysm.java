package ai.cataclysm;

import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class HealerCataclysm extends Fighter
{
	// Через сколько секунд будет происходить лечение (по умолчанию 10 секунд)
	public int healTime = 10;

	private NpcInstance _master;
	private ScheduledFuture<?> _heal;

	public HealerCataclysm(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		_heal = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HealTask(), healTime * 1000, healTime * 1000);

		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}

	public class HealTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor == null || actor.isDead())
				return;

			if(getMaster() == null)
			{
				actor.deleteMe();
				return;
			}

			actor.doCast(SkillTable.getInstance().getInfo(4027, 1), getMaster(), true);
		}
	}

	public void setMaster(NpcInstance actor)
	{
		_master = actor;
	}

	public NpcInstance getMaster()
	{
		return _master;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(_heal != null)
		{
			_heal.cancel(true);
			_heal = null;
		}

		super.onEvtDead(killer);
	}
}