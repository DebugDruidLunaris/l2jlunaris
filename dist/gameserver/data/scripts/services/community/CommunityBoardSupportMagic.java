package services.community;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Race;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.tables.SkillTable;

public class CommunityBoardSupportMagic
{
	private final static int[][] _mageBuff = new int[][] 
	{
		// minlevel maxlevel skill skilllevel
		{ 6, 75, 4322, 1 }, // windwalk
		{ 6, 75, 4323, 1 }, // shield
		{ 6, 75, 5637, 1 }, // Magic Barrier 1
		{ 6, 75, 4328, 1 }, // blessthesoul
		{ 6, 75, 4329, 1 }, // acumen
		{ 6, 75, 4330, 1 }, // concentration
		{ 6, 75, 4331, 1 }, // empower
		{ 16, 34, 4338, 1 }, // life cubic
	};

	private final static int[][] _warrBuff = new int[][] 
	{
		// minlevel maxlevel skill skilllevel
		{ 6, 75, 4322, 1 }, // windwalk
		{ 6, 75, 4323, 1 }, // shield
		{ 6, 75, 5637, 1 }, // Magic Barrier 1
		{ 6, 75, 4324, 1 }, // btb
		{ 6, 75, 4325, 1 }, // vampirerage
		{ 6, 75, 4326, 1 }, // regeneration
		{ 6, 39, 4327, 1 }, // haste 1
		{ 40, 75, 5632, 1 }, // haste 2
		{ 16, 34, 4338, 1 }, // life cubic
	};

	private final static int[][] _summonBuff = new int[][] 
	{
		// minlevel maxlevel skill skilllevel
		{ 6, 75, 4322, 1 }, // windwalk
		{ 6, 75, 4323, 1 }, // shield
		{ 6, 75, 5637, 1 }, // Magic Barrier 1
		{ 6, 75, 4324, 1 }, // btb
		{ 6, 75, 4325, 1 }, // vampirerage
		{ 6, 75, 4326, 1 }, // regeneration
		{ 6, 75, 4328, 1 }, // blessthesoul
		{ 6, 75, 4329, 1 }, // acumen
		{ 6, 75, 4330, 1 }, // concentration
		{ 6, 75, 4331, 1 }, // empower
		{ 6, 39, 4327, 1 }, // haste 1
		{ 40, 75, 5632, 1 }, // haste 2
	};

	public static void doSupportMagic(Player activechar, boolean player, boolean servitor)
	{
		int _minlevel = 6;
		int _maxlevel = 75;
		if(activechar == null)
			return;
		
		String resultFail = (activechar.isLangRus() ? "Магия поддержки выдается игрокам от " + _minlevel + " до " + _maxlevel + " уровней." : "Magic support outstanding players from " + _minlevel + " to " + _maxlevel + " levels.");

		if(activechar.isCursedWeaponEquipped())
			return;

		if(activechar.getNetConnection().getBonus() <= 1)
		{
			activechar.scriptRequest(activechar.isLangRus() ? "Только игроки с премиум аккаунтом могут использовать магию поддержки. Желаете преобрести Премиум Аккаунт?" : "Only player premium account can use support magic. Do you want buy Premium Account?", "services.RateBonus:list", new Object[0]);
			activechar.sendMessage(activechar.isLangRus() ? "Только игроки с премиум аккаунтом могут использовать магию поддержки." : "Only player premium account can use support magic.");
			return;
		}
		if(activechar.getLevel() < _minlevel)
		{
			activechar.sendMessage(activechar.isLangRus() ? "Ваш уровень слишком мал для использования магии поддержки." : "Your level is too low to use support magic.");
			activechar.sendMessage(resultFail);
			return;
		}
		if(activechar.getLevel() > _maxlevel)
		{
			activechar.sendMessage(activechar.isLangRus() ? "Ваш уровень слишком велик для использования магии поддержки." : "Your level is too large to use support magic.");
			activechar.sendMessage(resultFail);
			return;
		}
		int lvl = activechar.getLevel();

		List<Creature> target = new ArrayList<Creature>();
				
		if(player)
		{
			target.add(activechar);

			if(!activechar.isMageClass() || activechar.getTemplate().race == Race.orc)
			{
				for(int[] buff : _warrBuff)
				{
					if(lvl >= buff[0] && lvl <= buff[1])
					{
						activechar.broadcastPacket(new MagicSkillUse(activechar, activechar, buff[2], buff[3], 0, 0));
						activechar.callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), target, true);
					}
				}
			}
			else
			{
				for(int[] buff : _mageBuff)
				{
					if(lvl >= buff[0] && lvl <= buff[1])
					{
						activechar.broadcastPacket(new MagicSkillUse(activechar, activechar, buff[2], buff[3], 0, 0));
						activechar.callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), target, true);
					}
				}
			}
		}

		List<Creature> pet = new ArrayList<Creature>();

		if(servitor && activechar.getPet() != null)
		{
			pet.add(activechar.getPet());

			for(int[] buff : _summonBuff)
			{
				if(lvl >= buff[0] && lvl <= buff[1])
				{
					activechar.broadcastPacket(new MagicSkillUse(activechar, activechar.getPet(), buff[2], buff[3], 0, 0));
					activechar.callSkill(SkillTable.getInstance().getInfo(buff[2], buff[3]), pet, true);
				}
			}
		}
	}
}