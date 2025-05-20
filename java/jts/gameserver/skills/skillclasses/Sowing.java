package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Manor;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.templates.StatsSet;

public class Sowing extends Skill
{
	public Sowing(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;

		Player player = (Player) activeChar;
		int seedId = player.getUseSeed();
		boolean altSeed = ItemHolder.getInstance().getTemplate(seedId).isAltSeed();

		// remove seed from inventory
		if(!player.getInventory().destroyItemByItemId(seedId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		player.sendPacket(SystemMessage2.removeItems(seedId, 1L));

		for(Creature target : targets)
			if(target != null)
			{
				MonsterInstance monster = (MonsterInstance) target;
				if(monster.isSeeded())
				{
					continue;
				}

				// обработка
				double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;

				double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
				double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());

				// Штраф, на разницу уровней между мобом и игроком
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
				{
					SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
				}

				// Штраф, на разницу уровней между семечкой и мобом
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET)
				{
					SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;
				}

				if(altSeed)
				{
					SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;
				}

				// Минимальный шанс успеха всегда 1%
				if(SuccessRate < 1)
				{
					SuccessRate = 1;
				}

				if(Config.SKILLS_CHANCE_SHOW && activeChar.isPlayer() && activeChar.getPlayer().getVarB("SkillsHideChance"))
				{
					if(activeChar.getPlayer() != null)
					{
						activeChar.getPlayer().sendMessage(new CustomMessage("jts.gameserver.skills.skillclasses.Sowing.Chance", activeChar.getPlayer()).addNumber((long) SuccessRate));
					}
				}

				if(Rnd.chance(SuccessRate) && monster.setSeeded(player, seedId, altSeed))
				{
					activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
				}
				else
				{
					activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
				}
			}
	}
}