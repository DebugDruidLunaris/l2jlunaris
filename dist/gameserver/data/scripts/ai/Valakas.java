package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Zone;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;
import bosses.ValakasManager;

public class Valakas extends DefaultAI
{
	// Self skills
	final Skill s_lava_skin = getSkill(4680, 1), s_fear = getSkill(4689, 1), s_defence_down = getSkill(5864, 1), s_berserk = getSkill(5865, 1),
			s_regen = getSkill(4691, 1);

	// Offensive damage skills
	final Skill s_tremple_left = getSkill(4681, 1), s_tremple_right = getSkill(4682, 1), s_tail_stomp_a = getSkill(4685, 1),
			s_tail_lash = getSkill(4688, 1), s_meteor = getSkill(4690, 1), s_breath_low = getSkill(4683, 1), s_breath_high = getSkill(4684, 1);

	// Offensive percentage skills
	final Skill s_destroy_body = getSkill(5860, 1), s_destroy_soul = getSkill(5861, 1), s_destroy_body2 = getSkill(5862, 1),
			s_destroy_soul2 = getSkill(5863, 1);

	// Timers
	private long defenceDownTimer = Long.MAX_VALUE;

	// Timer reuses
	private final long defenceDownReuse = 120000L;
    //Вестника Невитта и длительность его спавна
    private static int INVOKER_NEVIT_HERALD = 4326;
    private static final int DESPAWN_TIME = 180 * 60 * 1000; // 3 часа = 180 минут
	// Vars
	private double _rangedAttacksIndex, _counterAttackIndex, _attacksIndex;
	private int _hpStage = 0;
	private List<NpcInstance> minions = new ArrayList<NpcInstance>();

	public Valakas(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		ValakasManager.setLastAttackTime();
		for(Playable p : ValakasManager.getZone().getInsidePlayables())
			notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 1);
		if(damage > 100)
			if(attacker.getDistance(actor) > 400)
				_rangedAttacksIndex += damage / 1000D;
			else
				_counterAttackIndex += damage / 1000D;
		_attacksIndex += damage / 1000D;
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		if((target = prepareTarget()) == null)
			return false;

		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

		double distance = actor.getDistance(target);

		// Buffs and stats
		double chp = actor.getCurrentHpPercents();
		if(_hpStage == 0)
		{
			actor.altOnMagicUseTimer(actor, getSkill(4691, 1));
			_hpStage = 1;
		}
		else if(chp < 80 && _hpStage == 1)
		{
			actor.altOnMagicUseTimer(actor, getSkill(4691, 2));
			defenceDownTimer = System.currentTimeMillis();
			_hpStage = 2;
		}
		else if(chp < 50 && _hpStage == 2)
		{
			actor.altOnMagicUseTimer(actor, getSkill(4691, 3));
			_hpStage = 3;
		}
		else if(chp < 30 && _hpStage == 3)
		{
			actor.altOnMagicUseTimer(actor, getSkill(4691, 4));
			_hpStage = 4;
		}
		else if(chp < 10 && _hpStage == 4)
		{
			actor.altOnMagicUseTimer(actor, getSkill(4691, 5));
			_hpStage = 5;
		}

		// Minions spawn
		if(getAliveMinionsCount() < 100 && Rnd.chance(5))
		{
			NpcInstance minion = Functions.spawn(Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()), 29029); // Valakas Minions
			minions.add(minion);
			ValakasManager.addValakasMinion(minion);
		}

		// Tactical Movements
		if(_counterAttackIndex > 2000)
		{
			ValakasManager.broadcastScreenMessage(NpcString.VALAKAS_HEIGHTENED_BY_COUNTERATTACKS);
			_counterAttackIndex = 0;
			return chooseTaskAndTargets(s_berserk, actor, 0);
		}
		else if(_rangedAttacksIndex > 2000)
		{
			if(Rnd.chance(60))
			{
				Creature randomHated = actor.getAggroList().getRandomHated();
				if(randomHated != null)
				{
					setAttackTarget(randomHated);
					actor.startConfused();
					ThreadPoolManager.getInstance().schedule(new RunnableImpl(){
						@Override
						public void runImpl() throws Exception
						{
							NpcInstance actor = getActor();
							if(actor != null)
								actor.stopConfused();
							_madnessTask = null;
						}
					}, 20000L);
				}
				ValakasManager.broadcastScreenMessage(NpcString.VALAKAS_RANGED_ATTACKS_ENRAGED_TARGET_FREE);
				_rangedAttacksIndex = 0;
			}
			else
			{
				ValakasManager.broadcastScreenMessage(NpcString.VALAKAS_RANGED_ATTACKS_PROVOKED);
				_rangedAttacksIndex = 0;
				return chooseTaskAndTargets(s_berserk, actor, 0);
			}
		}
		else if(_attacksIndex > 3000)
		{
			ValakasManager.broadcastScreenMessage(NpcString.VALAKAS_PDEF_ISM_DECREACED_SLICED_DASH);
			_attacksIndex = 0;
			return chooseTaskAndTargets(s_defence_down, actor, 0);
		}
		else if(defenceDownTimer < System.currentTimeMillis())
		{
			ValakasManager.broadcastScreenMessage(NpcString.VALAKAS_FINDS_YOU_ATTACKS_ANNOYING_SILENCE);
			defenceDownTimer = System.currentTimeMillis() + defenceDownReuse + Rnd.get(60) * 1000L;
			return chooseTaskAndTargets(s_fear, target, distance);
		}

		// Basic Attack
		if(Rnd.chance(50))
			return chooseTaskAndTargets(Rnd.chance(50) ? s_tremple_left : s_tremple_right, target, distance);

		// Stage based skill attacks
		Map<Skill, Integer> d_skill = new HashMap<Skill, Integer>();
		switch(_hpStage)
		{
			case 1:
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
				break;
			case 2:
			case 3:
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_breath_high);
				addDesiredSkill(d_skill, target, distance, s_tail_lash);
				addDesiredSkill(d_skill, target, distance, s_destroy_body);
				addDesiredSkill(d_skill, target, distance, s_destroy_soul);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
				break;
			case 4:
			case 5:
				addDesiredSkill(d_skill, target, distance, s_breath_low);
				addDesiredSkill(d_skill, target, distance, s_tail_stomp_a);
				addDesiredSkill(d_skill, target, distance, s_breath_high);
				addDesiredSkill(d_skill, target, distance, s_tail_lash);
				addDesiredSkill(d_skill, target, distance, s_destroy_body);
				addDesiredSkill(d_skill, target, distance, s_destroy_soul);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear);
				addDesiredSkill(d_skill, target, distance, Rnd.chance(60) ? s_destroy_soul2 : s_destroy_body2);
				break;
		}

		Skill r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.isOffensive())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	@Override
	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		// Lava buff
		if(actor.isInZone(Zone.ZoneType.poison))
			if(actor.getEffectList() != null && actor.getEffectList().getEffectsBySkill(s_lava_skin) == null)
				actor.altOnMagicUseTimer(actor, s_lava_skin);
		super.thinkAttack();
	}

	private Skill getSkill(int id, int level)
	{
		return SkillTable.getInstance().getInfo(id, level);
	}

	private int getAliveMinionsCount()
	{
		int i = 0;
		for(NpcInstance n : minions)
			if(n != null && !n.isDead())
				i++;
		return i;
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if(minions != null && !minions.isEmpty())
			for(NpcInstance n : minions)
				n.deleteMe();
        //Спавним Вестника Невитта
        try
        {
            NpcInstance HeralGiran = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralGiran.spawnMe(new Location(82152, 148488, -3492, 60699));

            NpcInstance HeralAden = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralAden.spawnMe(new Location(147048, 25608, -2038, 16383));

            NpcInstance HeralGoddart = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralGoddart.spawnMe(new Location(147384, -55400, -2759, 57343));

            NpcInstance HeralRune = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralRune.spawnMe(new Location(42904, -47912, -822, 49151));

            NpcInstance HeralDion = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralDion.spawnMe(new Location(15736, 142744, -2731, 16383));

            NpcInstance HeralOren = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralOren.spawnMe(new Location(82120, 53224, -1521, 16383));

            NpcInstance HeralGludio = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralGludio.spawnMe(new Location(-14168, 121192, -3014, 16383));

            NpcInstance HeralGludin = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralGludin.spawnMe(new Location(-80680, 150472, -3040, 44090));

            NpcInstance HeralSchuttgart = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralSchuttgart.spawnMe(new Location(87608, -141320, -1364, 49151));

            NpcInstance HeralHein = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralHein.spawnMe(new Location(110552, 219848, -3696, 57343));

            NpcInstance HeralHunter = NpcHolder.getInstance().getTemplate(INVOKER_NEVIT_HERALD).getNewInstance();
            HeralHunter.spawnMe(new Location(116824, 77400, -2722, 40959));


            ThreadPoolManager.getInstance().schedule(
                    new DeSpawnScheduleTimerTask(HeralGiran, HeralAden, HeralGoddart, HeralRune, HeralDion, HeralOren,
                            HeralGludio, HeralGludin, HeralSchuttgart, HeralHein, HeralHunter), DESPAWN_TIME);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



		super.onEvtDead(killer);
	}
	 // По истечению времени удаляем Вестника Невитта
    class DeSpawnScheduleTimerTask extends RunnableImpl
    {
        final NpcInstance HeralGiran;
        final NpcInstance HeralAden;
        final NpcInstance HeralGoddart;
        final NpcInstance HeralRune;
        final NpcInstance HeralDion;
        final NpcInstance HeralOren;
        final NpcInstance HeralGludio;
        final NpcInstance HeralGludin;
        final NpcInstance HeralSchuttgart;
        final NpcInstance HeralHein;
        final NpcInstance HeralHunter;

        public DeSpawnScheduleTimerTask(NpcInstance HeralGiran, NpcInstance HeralAden, NpcInstance HeralGoddart, NpcInstance HeralRune,
                                        NpcInstance HeralDion, NpcInstance HeralOren, NpcInstance HeralGludio, NpcInstance HeralGludin,
                                        NpcInstance HeralSchuttgart, NpcInstance HeralHein, NpcInstance HeralHunter)
        {
            this.HeralGiran = HeralGiran;
            this.HeralAden = HeralAden;
            this.HeralGoddart = HeralGoddart;
            this.HeralRune = HeralRune;
            this.HeralDion = HeralDion;
            this.HeralOren = HeralOren;
            this.HeralGludio = HeralGludio;
            this.HeralGludin = HeralGludin;
            this.HeralSchuttgart = HeralSchuttgart;
            this.HeralHein = HeralHein;
            this.HeralHunter = HeralHunter;
        }

        @Override
        public void runImpl()
        {
            HeralGiran.deleteMe();
            HeralAden.deleteMe();
            HeralGoddart.deleteMe();
            HeralRune.deleteMe();
            HeralDion.deleteMe();
            HeralOren.deleteMe();
            HeralGludio.deleteMe();
            HeralGludin.deleteMe();
            HeralSchuttgart.deleteMe();
            HeralHein.deleteMe();
            HeralHunter.deleteMe();
        }
    }


}