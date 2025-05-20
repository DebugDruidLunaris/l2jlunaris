package ai.residences.clanhall;

import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class MatchCleric extends MatchFighter
{
	public static final Skill HEAL = SkillTable.getInstance().getInfo(4056, 6);

	public MatchCleric(NpcInstance actor)
	{
		super(actor);
	}

	public void heal()
	{
		NpcInstance actor = getActor();
		addTaskCast(actor, HEAL);
		doTask();
	}
}