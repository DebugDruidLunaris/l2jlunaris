package ai.SkyshadowMeadow;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.NpcUtils;

public class Fire extends DefaultAI
{
	private static final int FEED = 18933;
	private boolean _firstTime = true;
	private long _wait_timeout = System.currentTimeMillis() + Rnd.get(120, 240) * 1000;

	public Fire(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		// При простое загораемся через 210 секунд
		if(_wait_timeout < System.currentTimeMillis())
		{
			// Ставим следущее загорание через 180-240 секунд
			_wait_timeout = (System.currentTimeMillis() + Rnd.get(120, 240) * 1000);

			if(actor.getNpcState() == 0 || actor.getNpcState() == 2)
				actor.setNpcState((byte) 1); // Загорелись
			else if(actor.getNpcState() == 1)
				actor.setNpcState((byte) 2); // Затушились
		}

		for(NpcInstance npc : actor.getAroundNpc(150, 150))
		{
			if(npc.isMonster() && npc.getNpcId() == 18908)
			{
				if(_firstTime)
				{
					// Включаем паузу что бы не спавнилось много Катлов.
					_firstTime = false;
					if(actor.getNpcState() < 1)
						actor.setNpcState((byte) 1); // Зажигаем кастер.
					NpcUtils.spawnSingle(FEED, new Location(actor.getX(), actor.getY(), actor.getZ()), 0);
					ThreadPoolManager.getInstance().schedule(new SpawnStart(), 20000); // Время паузы
				}
			}
		}
		return true;
	}

	private class SpawnStart extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor == null)
				return;

			// Выключаем паузу
			_firstTime = true;
		}
	}
}