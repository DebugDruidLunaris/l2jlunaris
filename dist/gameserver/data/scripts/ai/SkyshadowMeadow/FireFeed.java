package ai.SkyshadowMeadow;

import jts.commons.util.Rnd;
import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

import org.apache.log4j.Logger;

public class FireFeed extends DefaultAI
{
	protected static Logger _log = Logger.getLogger(FireFeed.class.getName());
	private long _wait_timeout = System.currentTimeMillis() + Rnd.get(10, 30) * 1000;

	public FireFeed(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_wait_timeout < System.currentTimeMillis())
			actor.decayMe();

		return true;
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		if(skill.getId() != 9075)
			return;

		NpcInstance actor = getActor();
		if(actor == null)
			return;

		actor.doCast(SkillTable.getInstance().getInfo(6688, 1), caster, true);
	}
}