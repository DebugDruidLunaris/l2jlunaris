package ai.residences.clanhall;

import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class MatchTrief extends MatchFighter
{
	public static final Skill HOLD = SkillTable.getInstance().getInfo(4047, 6);

	public MatchTrief(NpcInstance actor)
	{
		super(actor);
	}

	public void hold()
	{
		NpcInstance actor = getActor();
		addTaskCast(actor, HOLD);
		doTask();
	}
}