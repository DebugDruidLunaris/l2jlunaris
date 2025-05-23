package ai;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.Location;

public class Tears extends DefaultAI
{
	private class DeSpawnTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			for(NpcInstance npc : spawns)
				if(npc != null)
					npc.deleteMe();
			spawns.clear();
			despawnTask = null;
		}
	}

	private class SpawnMobsTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			spawnMobs();
			spawnTask = null;
		}
	}

	final Skill Invincible;
	final Skill Freezing;

	private static final int Water_Dragon_Scale = 2369;
	private static final int Tears_Copy = 25535;

	ScheduledFuture<?> spawnTask;
	ScheduledFuture<?> despawnTask;

	List<NpcInstance> spawns = new ArrayList<NpcInstance>();

	private boolean _isUsedInvincible = false;

	private int _scale_count = 0;
	private long _last_scale_time = 0;

	public Tears(NpcInstance actor)
	{
		super(actor);

		TIntObjectHashMap<Skill> skills = getActor().getTemplate().getSkills();

		Invincible = skills.get(5420);
		Freezing = skills.get(5238);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		NpcInstance actor = getActor();
		if(actor.isDead() || skill == null || caster == null)
			return;

		if(System.currentTimeMillis() - _last_scale_time > 5000)
			_scale_count = 0;

		if(skill.getId() == Water_Dragon_Scale)
		{
			_scale_count++;
			_last_scale_time = System.currentTimeMillis();
		}

		Player player = caster.getPlayer();
		if(player == null)
			return;

		int count = 1;
		Party party = player.getParty();
		if(party != null)
			count = party.getMemberCount();

		// Снимаем неуязвимость
		if(_scale_count >= count)
		{
			_scale_count = 0;
			actor.getEffectList().stopEffect(Invincible);
		}
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
		double actor_hp_precent = actor.getCurrentHpPercents();
		int rnd_per = Rnd.get(100);

		if(actor_hp_precent < 15 && !_isUsedInvincible)
		{
			_isUsedInvincible = true;
			addTaskBuff(actor, Invincible);
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.Tears");
			return true;
		}

		if(rnd_per < 5 && spawnTask == null && despawnTask == null)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 5441, 1, 3000, 0));
			spawnTask = ThreadPoolManager.getInstance().schedule(new SpawnMobsTask(), 3000);
			return true;
		}

		if(!actor.isAMuted() && rnd_per < 75)
			return chooseTaskAndTargets(null, target, distance);

		return chooseTaskAndTargets(Freezing, target, distance);
	}

	private void spawnMobs()
	{
		NpcInstance actor = getActor();

		Location pos;
		Creature hated;

		// Спавним 9 копий
		for(int i = 0; i < 9; i++)
			try
			{
				pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getGeoIndex());
				SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(Tears_Copy));
				sp.setLoc(pos);
				sp.setReflection(actor.getReflection());
				NpcInstance copy = sp.doSpawn(true);
				spawns.add(copy);

				// Атакуем случайную цель
				hated = actor.getAggroList().getRandomHated();
				if(hated != null)
					copy.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		// Прячемся среди них
		pos = Location.findPointToStay(144298, 154420, -11854, 300, 320, actor.getReflectionId());
		actor.teleToLocation(pos);

		// Атакуем случайную цель
		hated = actor.getAggroList().getRandomHated();
		if(hated != null)
			actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, Rnd.get(1, 100));

		if(despawnTask != null)
			despawnTask.cancel(false);
		despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 30000);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}