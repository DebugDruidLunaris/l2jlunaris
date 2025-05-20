package jts.gameserver.network.clientpackets;

import jts.commons.util.Rnd;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.actor.instances.player.ShortCut;
import jts.gameserver.model.base.EnchantSkillLearn;
import jts.gameserver.network.serverpackets.ExEnchantSkillInfo;
import jts.gameserver.network.serverpackets.ExEnchantSkillResult;
import jts.gameserver.network.serverpackets.ShortCutRegister;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.skills.TimeStamp;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.tables.SkillTreeTable;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

public class RequestExEnchantSkill extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getTransformation() != 0)
		{
			if (activeChar.isLangRus())
			{
				activeChar.sendMessage("Вы должны выйти из режима трансформации.");
			}
			else
			{
				activeChar.sendMessage("You must leave transformation mode first.");
			}
			return;
		}

		if(activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4)
		{
			if (activeChar.isLangRus())
			{
				activeChar.sendMessage("Вы должны сначала получить 3 профессию.");
			}
			else
			{
				activeChar.sendMessage("You must have 3rd class change quest completed.");
			}
			return;
		}

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
		if(sl == null)
			return;

		int slevel = activeChar.getSkillLevel(_skillId);
		if(slevel == -1)
			return;

		int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel());

		// already knows the skill with this level
		if(slevel >= enchantLevel)
			return;

		// Можем ли мы перейти с текущего уровня скилла на данную заточку
		if(slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1)
		{
			activeChar.sendMessage("Incorrect enchant level.");
			return;
		}

		Skill skill = SkillTable.getInstance().getInfo(_skillId, enchantLevel);
		if(skill == null)
		{
			activeChar.sendMessage("Internal error: not found skill level");
			return;
		}

		int[] cost = sl.getCost();
		int requiredSp = cost[1] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int requiredAdena = cost[0] * SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
		int rate = sl.getRate(activeChar);

		if(activeChar.getSp() < requiredSp)
		{
			sendPacket(Msg.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(_skillLvl % 100 == 1) // only first lvl requires book (101, 201, 301 ...)
		{
			if(Functions.getItemCount(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK) == 0)
			{
				activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
				return;
			}
			Functions.removeItem(activeChar, SkillTreeTable.NORMAL_ENCHANT_BOOK, 1);
		}

		TimeStamp ts = null;
			ts = activeChar.getSkillReuse(activeChar.getKnownSkill(_skillId));
		
		if(Rnd.chance(rate))
		{
			activeChar.addExpAndSp(0, -1 * requiredSp);
			Functions.removeItem(activeChar, 57, requiredAdena);
			activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(requiredSp), new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new SkillList(activeChar), new ExEnchantSkillResult(1));
			Log.add(activeChar.getName() + "|Successfully enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
			Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", new String[] { "Successfully enchanted", "skill ID:" + this._skillId + " skill level: " + this._skillLvl + " and rate in % " + rate + "" });
		}
		else
		{
			skill = SkillTable.getInstance().getInfo(_skillId, sl.getBaseLevel());
			activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0));
			Log.add(activeChar.getName() + "|Failed to enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
			Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", new String[] { "Failed to enchant", "skill ID:" + this._skillId + " skill level: " + this._skillLvl + " and rate in % " + rate + "" });
		}
		activeChar.addSkill(skill, true);
		if (ts != null && ts.hasNotPassed())
			activeChar.disableSkill(skill, ts.getReuseCurrent());
		updateSkillShortcuts(activeChar, _skillId, _skillLvl);
		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
	}

	protected static void updateSkillShortcuts(Player player, int skillId, int skillLevel)
	{
		for(ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == skillId && sc.getType() == ShortCut.TYPE_SKILL)
			{
				ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				player.sendPacket(new ShortCutRegister(player, newsc));
				player.registerShortCut(newsc);
			}
	}
}