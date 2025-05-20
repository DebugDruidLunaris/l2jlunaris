package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.AggroList.AggroInfo;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.templates.StatsSet;

public class ShiftAggression extends Skill
{
	public ShiftAggression(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(activeChar.getPlayer() == null)
			return;

		for(Creature target : targets)
			if(target != null)
			{
				if(!target.isPlayer())
					continue;

				Player player = (Player) target;

				for(NpcInstance npc : World.getAroundNpc(activeChar, getSkillRadius(), getSkillRadius()))
				{
					AggroInfo ai = npc.getAggroList().get(activeChar);
					if(ai == null)
						continue;
					npc.getAggroList().addDamageHate(player, 0, ai.hate);
					npc.getAggroList().remove(activeChar, true);
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}