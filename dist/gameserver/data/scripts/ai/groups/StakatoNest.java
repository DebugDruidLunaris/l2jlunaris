package ai.groups;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.Fighter;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.MinionList;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.Zone;
import jts.gameserver.model.instances.MinionInstance;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.PositionUtils;
import jts.gameserver.utils.ReflectionUtils;

import org.apache.commons.lang3.ArrayUtils;

public class StakatoNest extends Fighter
{
	private static final int[] BIZARRE_COCOON = { 18793, 18794, 18795, 18796, 18797, 18798 };
	private static final int CANNIBALISTIC_STAKATO_LEADER = 22625;
	private static final int SPIKE_STAKATO_NURSE = 22630;
	private static final int SPIKE_STAKATO_NURSE_CHANGED = 22631;
	private static final int SPIKED_STAKATO_BABY = 22632;
	private static final int SPIKED_STAKATO_CAPTAIN = 22629;
	private static final int FEMALE_SPIKED_STAKATO = 22620;
	private static final int MALE_SPIKED_STAKATO = 22621;
	private static final int MALE_SPIKED_STAKATO_2 = 22622;
	private static final int SPIKED_STAKATO_GUARD = 22619;
	private static final int SKILL_GROWTH_ACCELERATOR = 2905;
	private static final int CANNIBALISTIC_STAKATO_CHIEF = 25667;
	private static final int QUEEN_SHYEED = 25671;

	private static final int FAIL_COCOON_CHANCE = 8;
	private static final int ABSORB_MINION_CHANCE = 10;

	// Queen Shyeed Management
	private static Zone _zone_mob_buff = ReflectionUtils.getZone("[stakato_mob_buff]");
	private static Zone _zone_mob_buff_pc_display = ReflectionUtils.getZone("[stakato_mob_buff_display]");
	private static Zone _zone_pc_buff = ReflectionUtils.getZone("[stakato_pc_buff]");
	private static boolean _debuffed = false;

	public StakatoNest(NpcInstance actor)
	{
		super(actor);
		if(ArrayUtils.contains(BIZARRE_COCOON, actor.getNpcId()))
		{
			actor.setIsInvul(true);
			actor.startImmobilized();
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor.getNpcId() != QUEEN_SHYEED)
		{
			super.onEvtSpawn();
			return;
		}
		if(!_debuffed)
		{
			_debuffed = true;
			_zone_mob_buff.setActive(true);
			_zone_mob_buff_pc_display.setActive(true);
			_zone_pc_buff.setActive(false);
		}
		for(Player player : World.getAroundPlayers(actor))
			if(player != null)
				player.sendPacket(Msg.SHYEED_S_ROAR_FILLED_WITH_WRATH_RINGS_THROUGHOUT_THE_STAKATO_NEST);
		super.onEvtSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		MonsterInstance _mob = (MonsterInstance) actor;

		if(_mob.getNpcId() == CANNIBALISTIC_STAKATO_LEADER && Rnd.chance(ABSORB_MINION_CHANCE) && _mob.getCurrentHpPercents() < 30)
		{
			MonsterInstance _follower = getAliveMinion(actor);

			if(_follower != null && _follower.getCurrentHpPercents() > 30)
			{
				_mob.abortAttack(true, false);
				_mob.abortCast(true, false);
				_mob.setHeading(PositionUtils.getHeadingTo(_mob, _follower));
				_mob.doCast(SkillTable.getInstance().getInfo(4485, 1), _follower, false);
				_mob.setCurrentHp(_mob.getCurrentHp() + _follower.getCurrentHp(), false);
				_follower.doDie(_follower);
				_follower.deleteMe();
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();

		MinionInstance _minion = getAliveMinion(actor);
		MonsterInstance _leader = null;

		switch(actor.getNpcId())
		{
			case SPIKE_STAKATO_NURSE:
				if(_minion == null)
					break;
				actor.broadcastPacket(new MagicSkillUse(actor, 2046, 1, 1000, 0));
				for(int i = 0; i < 3; i++)
					spawnMonster(_minion, killer, SPIKED_STAKATO_CAPTAIN);
				break;
			case SPIKED_STAKATO_BABY:
				_leader = ((MinionInstance) actor).getLeader();
				if(_leader != null && !_leader.isDead())
					ThreadPoolManager.getInstance().schedule(new ChangeMonster(SPIKE_STAKATO_NURSE_CHANGED, actor, killer), 3000L);
				break;
			case MALE_SPIKED_STAKATO:
				if(_minion == null)
					break;
				actor.broadcastPacket(new MagicSkillUse(actor, 2046, 1, 1000, 0));
				for(int i = 0; i < 3; i++)
					spawnMonster(_minion, killer, SPIKED_STAKATO_GUARD);
				break;
			case FEMALE_SPIKED_STAKATO:
				_leader = ((MinionInstance) actor).getLeader();
				if(_leader != null && !_leader.isDead())
					ThreadPoolManager.getInstance().schedule(new ChangeMonster(MALE_SPIKED_STAKATO_2, actor, killer), 3000L);
				break;
			/*
			case CANNIBALISTIC_STAKATO_CHIEF:
			if(killer.isPlayer())
			if(killer.getPlayer().getParty() != null)
			{
				List<L2Player> party = killer.getPlayer().getParty().getPartyMembers();
				for(L2Player member : party)
					giveCocoon(member);
			}
			else
				giveCocoon(killer.getPlayer());
			break;
			 */
			case QUEEN_SHYEED:
				if(_debuffed)
				{
					_debuffed = false;
					_zone_pc_buff.setActive(true);
					_zone_mob_buff.setActive(false);
					_zone_mob_buff_pc_display.setActive(false);
				}
				break;
			default:
				break;
		}
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(actor == null || !ArrayUtils.contains(BIZARRE_COCOON, actor.getNpcId()) || caster == null || skill.getId() != SKILL_GROWTH_ACCELERATOR)
		{
			super.onEvtSeeSpell(skill, caster);
			return;
		}
		if(Rnd.chance(FAIL_COCOON_CHANCE))
		{
			caster.getPlayer().sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		actor.doDie(null);
		actor.endDecayTask();
		try
		{
			NpcInstance mob = NpcHolder.getInstance().getTemplate(CANNIBALISTIC_STAKATO_CHIEF).getNewInstance();
			mob.setSpawnedLoc(actor.getLoc());
			mob.setReflection(actor.getReflection());
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
			mob.spawnMe(mob.getSpawnedLoc());
			mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster.getPlayer(), Rnd.get(1, 100));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		super.onEvtSeeSpell(skill, caster);
	}

	private class ChangeMonster extends RunnableImpl
	{
		private int _monsterId;
		private Creature _killer;
		private NpcInstance _npc;

		public ChangeMonster(int mobId, NpcInstance npc, Creature killer)
		{
			_monsterId = mobId;
			_npc = npc;
			_killer = killer;
		}

		@Override
		public void runImpl()
		{
			spawnMonster(_npc, _killer, _monsterId);
		}
	}

	private MinionInstance getAliveMinion(NpcInstance npc)
	{
		MinionList ml = npc.getMinionList();
		if(ml != null && ml.hasAliveMinions())
			for(MinionInstance minion : ml.getAliveMinions())
				return minion;
		return null;
	}

	/*
		private void giveCocoon(L2Player player)
		{
			if(Rnd.chance(20))
				player.getInventory().addItem(LARGE_STAKATO_COCOON, 1);
			else
				player.getInventory().addItem(SMALL_STAKATO_COCOON, 1);
		}
	 */

	private void spawnMonster(NpcInstance actor, Creature killer, int mobId)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(mobId).getNewInstance();
			npc.setSpawnedLoc(actor.getSpawnedLoc());
			npc.setReflection(actor.getReflection());
			npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
			npc.spawnMe(actor.getSpawnedLoc());
			if(killer != null)
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(1, 100));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected boolean randomWalk()
	{
		return ArrayUtils.contains(BIZARRE_COCOON, getActor().getNpcId()) || getActor().getNpcId() == QUEEN_SHYEED ? false : true;
	}

	@Override
	protected boolean randomAnimation()
	{
		return ArrayUtils.contains(BIZARRE_COCOON, getActor().getNpcId()) ? false : true;
	}
}