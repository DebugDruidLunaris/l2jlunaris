package npc.model.birthday;

import java.util.concurrent.Future;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.templates.npc.NpcTemplate;

public class BirthDayCakeInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;
	private static final Skill SKILL = SkillTable.getInstance().getInfo(22035, 1);

	private class CastTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(Player player : World.getAroundPlayers(BirthDayCakeInstance.this, 500, 100))
			{
				if(player.getEffectList().getEffectsBySkill(SKILL) != null)
					continue;

				SKILL.getEffects(BirthDayCakeInstance.this, player, false, false);
			}
		}
	}

	private Future<?> _castTask;

	public BirthDayCakeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setTargetable(false);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		_castTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CastTask(), 1000L, 1000L);
	}

	@Override
	public void onDespawn()
	{
		super.onDespawn();

		_castTask.cancel(false);
		_castTask = null;
	}
}