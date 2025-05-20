package jts.gameserver.network.clientpackets;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.base.EnchantSkillLearn;
import jts.gameserver.network.serverpackets.ExEnchantSkillInfo;
import jts.gameserver.network.serverpackets.ExEnchantSkillResult;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.skills.TimeStamp;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.tables.SkillTreeTable;
import jts.gameserver.utils.Log;
import jts.gameserver.utils.Log_New;

public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
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

		int oldSkillLevel = activeChar.getSkillDisplayLevel(_skillId);
		if(oldSkillLevel == -1)
			return;

		if(_skillLvl != oldSkillLevel - 1 || _skillLvl / 100 != oldSkillLevel / 100)
			return;

		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, oldSkillLevel);
		if(sl == null)
			return;

		Skill newSkill;

		if(_skillLvl % 100 == 0)
		{
			_skillLvl = sl.getBaseLevel();
			newSkill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		}
		else
			newSkill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

		if(newSkill == null)
			return;

		if(Functions.getItemCount(activeChar, SkillTreeTable.UNTRAIN_ENCHANT_BOOK) == 0)
		{
			activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			return;
		}

		Functions.removeItem(activeChar, SkillTreeTable.UNTRAIN_ENCHANT_BOOK, 1);
		TimeStamp ts = null;
			ts = activeChar.getSkillReuse(activeChar.getKnownSkill(_skillId));

		activeChar.addExpAndSp(0, sl.getCost()[1] * sl.getCostMult());
		activeChar.addSkill(newSkill, true);
		if (ts != null && ts.hasNotPassed())
			activeChar.disableSkill(newSkill, ts.getReuseCurrent());
		if(_skillLvl > 100)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_has_been_decreased_by_1);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Untrain_of_enchant_skill_was_successful_Current_level_of_enchant_skill_S1_became_0_and_enchant_skill_will_be_initialized);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}

		Log.add(activeChar.getName() + "|Successfully untranes|" + _skillId + "|to+" + _skillLvl + "|---", "enchant_skills");
		Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "EnchantSkill", new String[] { "Successfully untrained", "skill ID:" + this._skillId + " skill level: " + this._skillLvl + " " });
		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, newSkill.getDisplayLevel()), ExEnchantSkillResult.SUCCESS, new SkillList(activeChar));
		RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
	}
}