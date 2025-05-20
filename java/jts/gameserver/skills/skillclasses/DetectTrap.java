package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.TrapInstance;
import jts.gameserver.network.serverpackets.NpcInfo;
import jts.gameserver.templates.StatsSet;

public class DetectTrap extends Skill
{
	public DetectTrap(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null && target.isTrap())
			{
				TrapInstance trap = (TrapInstance) target;
				if(trap.getLevel() <= getPower())
				{
					trap.setDetected(true);
					for(Player player : World.getAroundPlayers(trap))
						player.sendPacket(new NpcInfo(trap, player));
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}