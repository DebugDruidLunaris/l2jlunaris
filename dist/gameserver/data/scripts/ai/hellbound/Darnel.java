package ai.hellbound;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

import jts.commons.threading.RunnableImpl;
import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.idfactory.IdFactory;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.instances.TrapInstance;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;

public class Darnel extends DefaultAI
{
	private class TrapTask extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor.isDead())
				return;

			// Спавним 10 ловушек
			TrapInstance trap;
			for(int i = 0; i < 10; i++)
			{
				trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(13037), actor, trapSkills[Rnd.get(trapSkills.length)], new Location(Rnd.get(151896, 153608), Rnd.get(145032, 146808), -12584));
				trap.spawnMe();
			}
		}
	}

	final Skill[] trapSkills = new Skill[] {
			SkillTable.getInstance().getInfo(5267, 1),
			SkillTable.getInstance().getInfo(5268, 1),
			SkillTable.getInstance().getInfo(5269, 1),
			SkillTable.getInstance().getInfo(5270, 1) };

	final Skill Poison;
	final Skill Paralysis;

	public Darnel(NpcInstance actor)
	{
		super(actor);

		TIntObjectHashMap<Skill> skills = getActor().getTemplate().getSkills();

		Poison = skills.get(4182);
		Paralysis = skills.get(4189);
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

		int rnd_per = Rnd.get(100);

		if(rnd_per < 5)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 5440, 1, 3000, 0));
			ThreadPoolManager.getInstance().schedule(new TrapTask(), 3000);
			return true;
		}

		double distance = actor.getDistance(target);

		if(!actor.isAMuted() && rnd_per < 75)
			return chooseTaskAndTargets(null, target, distance);

		Map<Skill, Integer> d_skill = new HashMap<Skill, Integer>();

		addDesiredSkill(d_skill, target, distance, Poison);
		addDesiredSkill(d_skill, target, distance, Paralysis);

		Skill r_skill = selectTopSkill(d_skill);

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}