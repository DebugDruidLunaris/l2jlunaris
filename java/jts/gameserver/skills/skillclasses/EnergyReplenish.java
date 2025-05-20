package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.items.Inventory;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.StatsSet;

public class EnergyReplenish extends Skill
{
	private int _addEnergy;

	public EnergyReplenish(StatsSet set)
	{
		super(set);
		_addEnergy = set.getInteger("addEnergy");
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LBRACELET);
		if(item == null || item.getTemplate().getAgathionEnergy() - item.getAgathionEnergy() < _addEnergy)
		{
			player.sendPacket(SystemMsg.YOUR_ENERGY_CANNOT_BE_REPLENISHED_BECAUSE_CONDITIONS_ARE_NOT_MET);
			return false;
		}

		return true;
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature cha : targets)
		{
			cha.setAgathionEnergy(cha.getAgathionEnergy() + _addEnergy);
			cha.sendPacket(new SystemMessage2(SystemMsg.ENERGY_S1_REPLENISHED).addInteger(_addEnergy));
		}
	}
}