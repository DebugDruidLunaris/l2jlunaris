package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.reward.RewardItem;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.utils.ItemFunctions;

public class Sweep extends Skill
{
	public Sweep(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(isNotTargetAoE())
			return super.checkCondition(activeChar, target, forceUse, dontMove, first);

		if(target == null)
			return false;

		if(!target.isMonster() || !target.isDead())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(!((MonsterInstance) target).isSpoiled())
		{
			activeChar.sendPacket(Msg.SWEEPER_FAILED_TARGET_NOT_SPOILED);
			return false;
		}

		if(!((MonsterInstance) target).isSpoiled((Player) activeChar))
		{
			activeChar.sendPacket(Msg.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		Player player = (Player) activeChar;

		for(Creature targ : targets)
		{
			if(targ == null || !targ.isMonster() || !targ.isDead() || !((MonsterInstance) targ).isSpoiled())
				continue;

			MonsterInstance target = (MonsterInstance) targ;

			if(!target.isSpoiled(player))
			{
				activeChar.sendPacket(Msg.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
				continue;
			}

			List<RewardItem> items = target.takeSweep();

			if(items == null)
			{
				activeChar.getAI().setAttackTarget(null);
				target.endDecayTask();
				continue;
			}

			for(RewardItem item : items)
			{
				ItemInstance sweep = ItemFunctions.createItem(item.itemId);
				sweep.setCount(item.count);

				if(player.isInParty() && player.getParty().isDistributeSpoilLoot())
				{
					player.getParty().distributeItem(player, sweep, null);
					continue;
				}

				if(!player.getInventory().validateCapacity(sweep) || !player.getInventory().validateWeight(sweep))
				{
					sweep.dropToTheGround(player, target);
					continue;
				}

				player.getInventory().addItem(sweep);

				SystemMessage smsg;
				if(item.count == 1)
				{
					smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1);
					smsg.addItemName(item.itemId);
					player.sendPacket(smsg);
				}
				else
				{
					smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1);
					smsg.addItemName(item.itemId);
					smsg.addNumber(item.count);
					player.sendPacket(smsg);
				}
				if(player.isInParty())
					if(item.count == 1)
					{
						smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S2_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						player.getParty().broadcastToPartyMembers(player, smsg);
					}
					else
					{
						smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_3_S2_S_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						smsg.addNumber(item.count);
						player.getParty().broadcastToPartyMembers(player, smsg);
					}
			}

			activeChar.getAI().setAttackTarget(null);
			target.endDecayTask();
		}
	}
}