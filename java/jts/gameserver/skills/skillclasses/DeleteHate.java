package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.templates.StatsSet;

public class DeleteHate extends Skill
{
	public DeleteHate(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{

				if(target.isRaid())
				{
					continue;
				}

				if(getActivateRate() > 0)
				{
					if(Config.SKILLS_CHANCE_SHOW && activeChar.isPlayer() && ((Player) activeChar).getVarB("SkillsHideChance"))
					{
						activeChar.sendMessage(new CustomMessage("jts.gameserver.skills.Formulas.Chance", (Player) activeChar).addString(getName()).addNumber(getActivateRate()));
					}

					if(!Rnd.chance(getActivateRate()))
						return;
				}

				if(target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().clear(false);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}

				getEffects(activeChar, target, false, false);
			}
	}
}