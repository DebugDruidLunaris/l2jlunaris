package ai.Event.HuntingforSharks;

import jts.commons.util.Rnd;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.Fighter;
import jts.gameserver.model.Creature;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;

import org.apache.commons.lang3.ArrayUtils;

public class SharkMove extends Fighter
{
	protected Location[] _points;
	private int[] _teleporters = { 36610 };

	private int _lastPoint = 0;
	private boolean _firstThought = true;

	public SharkMove(NpcInstance actor)
	{
		super(actor);
		MAX_PURSUE_RANGE = Integer.MAX_VALUE - 10;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(target.isPlayable() && !target.isDead() && !target.isInvisible())
		{
			actor.getAggroList().addDamageHate(target, 0, 1);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		if(super.thinkActive())
			return true;

		if(!getActor().isMoving)
			startMoveTask();

		return true;
	}

	@Override
	protected void onEvtArrived()
	{
		startMoveTask();
		super.onEvtArrived();
	}

	private void startMoveTask()
	{
		NpcInstance npc = getActor();
		if(_firstThought)
		{
			_lastPoint = getIndex(Location.findNearest(npc, _points));
			_firstThought = false;
		}
		else
			_lastPoint++;
		if(_lastPoint >= _points.length)
		{
			_lastPoint = 0;
			if(ArrayUtils.contains(_teleporters, npc.getNpcId()))
				npc.teleToLocation(_points[_lastPoint]);
		}
		npc.setWalking();
		npc.isInWater();
		if(Rnd.chance(100))
			npc.altOnMagicUseTimer(npc, SkillTable.getInstance().getInfo(6757, 1));
		addTaskMove(Location.findPointToStay(_points[_lastPoint], 250, npc.getGeoIndex()), true);
		doTask();
	}

	private int getIndex(Location loc)
	{
		for(int i = 0; i < _points.length; i++)
			if(_points[i] == loc)
				return i;
		return 0;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		return false;
	}

	@Override
	protected void teleportHome() {}

	@Override
	protected void returnHome(boolean clearAggro, boolean teleport)
	{
		super.returnHome(clearAggro, teleport);
		clearTasks();
		_firstThought = true;
		startMoveTask();
	}
}