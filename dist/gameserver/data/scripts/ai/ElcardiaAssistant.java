package ai;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;
import quests._10293_SevenSignsForbiddenBook;
import quests._10294_SevenSignsMonasteryofSilence;
import quests._10295_SevenSignsSolinasTomb;
import quests._10296_SevenSignsPoweroftheSeal;

public class ElcardiaAssistant extends DefaultAI
{
	private boolean _thinking = false;
	private ScheduledFuture<?> _followTask;
	private long _chatTimer;
	private final Skill vampRage = SkillTable.getInstance().getInfo(6727, 1);
	private final Skill holyResist = SkillTable.getInstance().getInfo(6729, 1);
	private final Skill blessBlood = SkillTable.getInstance().getInfo(6725, 1);
	private final Skill recharge = SkillTable.getInstance().getInfo(6728, 1);
	private final Skill heal = SkillTable.getInstance().getInfo(6724, 1);

	public ElcardiaAssistant(NpcInstance actor)
	{
		super(actor);
		_chatTimer = System.currentTimeMillis() + 8000L;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	private Player getMaster()
	{
		if(!getActor().getReflection().getPlayers().isEmpty())
			return getActor().getReflection().getPlayers().get(0);
		return null;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		Creature following = actor.getFollowTarget();
		if(following == null || !actor.isFollow)
		{
			Player master = getMaster();
			if(master != null)
			{
				actor.setFollowTarget(master);
				actor.setRunning();
				actor.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, master, Config.ALT_FOLLOW_RANGE);
			}
		}
		super.thinkActive();
		return false;
	}

	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(_thinking || actor.isActionsDisabled() || actor.isAfraid() || actor.isDead() || actor.isMovementDisabled())
			return;

		_thinking = true;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || getIntention() == CtrlIntention.AI_INTENTION_IDLE))
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
				thinkFollow();
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			_thinking = false;
		}
	}

	protected void thinkFollow()
	{
		NpcInstance actor = getActor();

		Creature target = actor.getFollowTarget();

		//Находимся слишком далеко цели, либо цель не пригодна для следования, либо не можем перемещаться
		if(target == null || target.isAlikeDead() || actor.getDistance(target) > 4000 || actor.isMovementDisabled())
		{
			actor.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}

		//Уже следуем за этой целью
		if(actor.isFollow && actor.getFollowTarget() == target)
		{
			clientActionFailed();
			return;
		}

		//Находимся достаточно близко
		if(actor.isInRange(target, Config.ALT_FOLLOW_RANGE + 20))
			clientActionFailed();

		if(_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		_followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 250L);

		// -----------------
		Reflection ref = actor.getReflection();

		if(ref != null && _chatTimer < System.currentTimeMillis())
		{
			_chatTimer = System.currentTimeMillis() + 5000;
			Player masterplayer = target.getPlayer();
			Map<Skill, Integer> d_skill = new HashMap<Skill, Integer>();
			double distance = actor.getDistance(target);
			switch(ref.getInstancedZoneId())
			{
				case 156:
					QuestState qs = masterplayer.getQuestState(_10293_SevenSignsForbiddenBook.class);
					if(qs != null && !qs.isCompleted())
					{
						if(Rnd.chance(20))
							return;
						if(qs.getCond() == 1)
							Functions.npcSay(actor, NpcString.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK);
						else if(qs.getCond() == 2)
							Functions.npcSay(actor, NpcString.WHAT_TOOK_SO_LONG_I_WAITED_FOR_EVER);
						else if(qs.getCond() >= 5)
							if(Rnd.chance(50))
								Functions.npcSay(actor, NpcString.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE);
							else
								Functions.npcSay(actor, NpcString.AN_UNDERGROUND_LIBRARY);
					}
					break;
				case 151:
					QuestState qs2 = masterplayer.getQuestState(_10294_SevenSignsMonasteryofSilence.class);
					if(qs2 != null && !qs2.isCompleted())
						if(qs2.getCond() == 2)
						{
							if(Rnd.chance(20))
								if(Rnd.chance(70))
									Functions.npcSay(actor, NpcString.IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK);
								else
									Functions.npcSay(actor, NpcString.REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND);

							//skill use task
							if(target.getCurrentHpPercents() < 70)
								addDesiredSkill(d_skill, target, distance, heal);
							if(target.getCurrentMpPercents() < 50)
								addDesiredSkill(d_skill, target, distance, recharge);
							if(target.isInCombat())
								addDesiredSkill(d_skill, target, distance, blessBlood);

							addDesiredSkill(d_skill, target, distance, vampRage);
							addDesiredSkill(d_skill, target, distance, holyResist);

							Skill r_skill = selectTopSkill(d_skill);
							chooseTaskAndTargets(r_skill, target, distance);
							doTask();
						}
						else if(qs2.getCond() == 3)
							Functions.npcSay(actor, NpcString.YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN);
					QuestState qs3 = masterplayer.getQuestState(_10295_SevenSignsSolinasTomb.class);
					if(qs3 != null && !qs3.isCompleted())
						if(qs3.getCond() == 1)
						{
							if(Rnd.chance(20))
								if(Rnd.chance(30))
									Functions.npcSay(actor, NpcString.TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE);
								else if(Rnd.chance(30))
									Functions.npcSay(actor, NpcString.THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED);
								else
									Functions.npcSay(actor, NpcString.THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER);

							//skill use task
							if(target.getCurrentHpPercents() < 80)
								addDesiredSkill(d_skill, target, distance, heal);
							if(target.getCurrentMpPercents() < 70)
								addDesiredSkill(d_skill, target, distance, recharge);
							if(target.isInCombat())
								addDesiredSkill(d_skill, target, distance, blessBlood);

							addDesiredSkill(d_skill, target, distance, vampRage);
							addDesiredSkill(d_skill, target, distance, holyResist);

							Skill r_skill = selectTopSkill(d_skill);
							chooseTaskAndTargets(r_skill, target, distance);
							doTask();
						}
					QuestState qs4 = masterplayer.getQuestState(_10296_SevenSignsPoweroftheSeal.class);
					if(qs4 != null && !qs4.isCompleted())
						if(qs4.getCond() == 2)
						{
							//skill use task
							if(target.getCurrentHpPercents() < 80)
								addDesiredSkill(d_skill, target, distance, heal);
							if(target.getCurrentMpPercents() < 70)
								addDesiredSkill(d_skill, target, distance, recharge);
							if(target.isInCombat())
								addDesiredSkill(d_skill, target, distance, blessBlood);

							addDesiredSkill(d_skill, target, distance, vampRage);
							addDesiredSkill(d_skill, target, distance, holyResist);

							Skill r_skill = selectTopSkill(d_skill);
							chooseTaskAndTargets(r_skill, target, distance);
							doTask();
						}
					break;
				default:
					break;
			}
		}
	}

	protected class ThinkFollow extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor == null)
				return;

			Creature target = actor.getFollowTarget();

			if(target == null || actor.getDistance(target) > 4000)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.teleToLocation(120664, -86968, -3392);
				return;
			}

			if(!actor.isInRange(target, Config.ALT_FOLLOW_RANGE + 20) && (!actor.isFollow || actor.getFollowTarget() != target))
			{
				Location loc = new Location(target.getX() + Rnd.get(-60, 60), target.getY() + Rnd.get(-60, 60), target.getZ());
				actor.followToCharacter(loc, target, Config.ALT_FOLLOW_RANGE, false);
			}
			_followTask = ThreadPoolManager.getInstance().schedule(this, 250L);
		}
	}

	@Override
	public void addTaskAttack(Creature target) {}
}