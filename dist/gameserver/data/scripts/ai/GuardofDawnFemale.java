package ai;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;

public class GuardofDawnFemale extends DefaultAI
{
	private static final int _aggrorange = 300;
	private static final Skill _skill = SkillTable.getInstance().getInfo(5978, 1);
	private Location _locTele = null;
	private boolean noCheckPlayers = false;

	public GuardofDawnFemale(NpcInstance actor, Location telePoint)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = 200;
		setTelePoint(telePoint);
	}

	public class Teleportation extends RunnableImpl
	{

		Location _telePoint = null;
		Playable _target = null;

		public Teleportation(Location telePoint, Playable target)
		{
			_telePoint = telePoint;
			_target = target;
		}

		@Override
		public void runImpl()
		{
			_target.teleToLocation(_telePoint);
			noCheckPlayers = false;
		}
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();

		// проверяем игроков вокруг
		if(!noCheckPlayers)
			checkAroundPlayers(actor);

		return true;
	}

	private boolean checkAroundPlayers(NpcInstance actor)
	{
		for(Playable target : World.getAroundPlayables(actor, _aggrorange, _aggrorange))
			if(target != null && target.isPlayer() && !target.isSilentMoving() && !target.isInvul() && GeoEngine.canSeeTarget(actor, target, false) && PositionUtils.isFacing(actor, target, 150))
			{
				actor.doCast(_skill, target, true);
				Functions.npcSayCustomMessage(getActor(), "scripts.ai.GuardofDawnFemale");
				noCheckPlayers = true;
				ThreadPoolManager.getInstance().schedule(new Teleportation(getTelePoint(), target), 3000);
				return true;
			}
		return false;
	}

	private void setTelePoint(Location loc)
	{
		_locTele = loc;
	}

	private Location getTelePoint()
	{
		return _locTele;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected void thinkAttack() {}

	@Override
	protected void onIntentionAttack(Creature target) {}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro) {}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage) {}

}