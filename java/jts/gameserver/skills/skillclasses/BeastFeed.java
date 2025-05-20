package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.FeedableBeastInstance;
import jts.gameserver.templates.StatsSet;

public class BeastFeed extends Skill
{
	public BeastFeed(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(final Creature activeChar, List<Creature> targets)
	{
		for(final Creature target : targets)
			ThreadPoolManager.getInstance().execute(new RunnableImpl(){
				@Override
				public void runImpl() throws Exception
				{
					if(target instanceof FeedableBeastInstance)
						((FeedableBeastInstance) target).onSkillUse((Player) activeChar, _id);
				}
			});
	}
}