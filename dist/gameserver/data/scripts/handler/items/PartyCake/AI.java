package handler.items.PartyCake;

import jts.gameserver.ai.DefaultAI;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.tables.SkillTable;

public class AI extends DefaultAI
{
	public AI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		int skillId = 22035;
		for(Player player : World.getAroundPlayers(actor, 200, 200))
			if(player != null && !player.isInZonePeace() && player.getEffectList().getEffectsBySkillId(skillId) == null)
				actor.doCast(SkillTable.getInstance().getInfo(skillId, 1), player, true);
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}