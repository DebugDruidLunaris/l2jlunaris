package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.network.serverpackets.RecipeBookItemList;
import jts.gameserver.templates.StatsSet;

public class Craft extends Skill
{
	private final boolean _dwarven;

	public Craft(StatsSet set)
	{
		super(set);
		_dwarven = set.getBool("isDwarven");
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player p = (Player) activeChar;
		if(p.isInStoreMode() || p.isProcessingRequest())
			return false;

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		activeChar.sendPacket(new RecipeBookItemList((Player) activeChar, _dwarven));
	}
}