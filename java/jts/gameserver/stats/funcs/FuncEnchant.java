package jts.gameserver.stats.funcs;

import jts.gameserver.Config;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.stats.Env;
import jts.gameserver.stats.Stats;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.templates.item.ItemType;
import jts.gameserver.templates.item.WeaponTemplate.WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void calc(Env env)
	{
		ItemInstance item = (ItemInstance) owner;

		int enchant = item.getEnchantLevel();
		int overenchant = Math.max(0, enchant - 3);

		switch(stat)
		{
			case SHIELD_DEFENCE:
			case MAGIC_DEFENCE:
			case POWER_DEFENCE:
			{
				env.value += enchant + overenchant * 2;
				return;
			}

			case MAX_HP:
			{
				// D, Single - 8.45*Math.pow(overenchant,1.71)
				// D, Full - 12.36*Math.pow(overenchant,1.71)
				// C, Single - 11.4*Math.pow(overenchant,1.71)
				// C, Full - 17.08*Math.pow(overenchant,1.71)
				// B, Single - 13.31*Math.pow(overenchant,1.71)
				// B, Full - 19.97*Math.pow(overenchant,1.71)
				// A, Single - 15.09*Math.pow(overenchant,1.71)
				// A, Full - 22.59*Math.pow(overenchant,1.71)
				// S, Single - 16.27*Math.pow(overenchant,1.71)
				// S, Full - 24.08*Math.pow(overenchant,1.71)

				if(overenchant > 0)
				{
					double mult = 0;
					switch(item.getTemplate().getCrystalType().cry)
					{
						case ItemTemplate.CRYSTAL_D:
							mult = 8.45;
							break;
						case ItemTemplate.CRYSTAL_C:
							mult = 11.4;
							break;
						case ItemTemplate.CRYSTAL_B:
							mult = 13.31;
							break;
						case ItemTemplate.CRYSTAL_A:
							mult = 15.09;
							break;
						case ItemTemplate.CRYSTAL_S:
							mult = 16.27;
							break;
					}
					if(item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
						mult *= 1.5;
					env.value += mult * Math.pow(Math.min(Config.ARMOR_OVERENCHANT_HPBONUS_LIMIT, overenchant), 1.71);
				}
				return;
			}

			case MAGIC_ATTACK:
			{
				switch(item.getTemplate().getCrystalType().cry)
				{
					case ItemTemplate.CRYSTAL_S:
						env.value += 4 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_A:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_B:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_C:
						env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						env.value += 2 * (enchant + overenchant);
						break;
				}
				return;
			}

			case POWER_ATTACK:
			{
				ItemType itemType = item.getItemType();
				boolean isBow = itemType == WeaponType.BOW || itemType == WeaponType.CROSSBOW;
				boolean isSword = (itemType == WeaponType.DUALFIST || itemType == WeaponType.DUAL || itemType == WeaponType.BIGSWORD || itemType == WeaponType.SWORD || itemType == WeaponType.RAPIER || itemType == WeaponType.ANCIENTSWORD) && item.getTemplate().getBodyPart() == ItemTemplate.SLOT_LR_HAND;
				switch(item.getTemplate().getCrystalType().cry)
				{
					case ItemTemplate.CRYSTAL_S:
						if(isBow)
							env.value += 10 * (enchant + overenchant);
						else if(isSword)
							env.value += 6 * (enchant + overenchant);
						else
							env.value += 5 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_A:
						if(isBow)
							env.value += 8 * (enchant + overenchant);
						else if(isSword)
							env.value += 5 * (enchant + overenchant);
						else
							env.value += 4 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_B:
					case ItemTemplate.CRYSTAL_C:
						if(isBow)
							env.value += 6 * (enchant + overenchant);
						else if(isSword)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 3 * (enchant + overenchant);
						break;
					case ItemTemplate.CRYSTAL_D:
					case ItemTemplate.CRYSTAL_NONE:
						if(isBow)
							env.value += 4 * (enchant + overenchant);
						else
							env.value += 2 * (enchant + overenchant);
						break;
				}
			}
		}
	}
}