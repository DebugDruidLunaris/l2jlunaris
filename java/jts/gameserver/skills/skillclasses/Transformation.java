package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Zone;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.skills.SkillType;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.utils.ReflectionUtils;

public class Transformation extends Skill
{
	public final boolean useSummon;
	public final boolean isDisguise;
	public final String transformationName;

	public Transformation(StatsSet set)
	{
		super(set);
		useSummon = set.getBool("useSummon", false);
		isDisguise = set.getBool("isDisguise", false);
		transformationName = set.getString("transformationName", null);
	}

	@Override
	public boolean checkCondition(final Creature activeChar, final Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = target.getPlayer();

		if(player == null || player.getActiveWeaponFlagAttachment() != null)
			return false;

		if(player.getTransformation() != 0 && getId() != SKILL_TRANSFORM_DISPEL)
		{
			// Для всех скилов кроме Transform Dispel
			activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}

		// Нельзя использовать летающую трансформу на территории Aden, или слишком высоко/низко, или при вызванном пете/саммоне, или в инстансе
		if((getId() == SKILL_FINAL_FLYING_FORM || getId() == SKILL_AURA_BIRD_FALCON || getId() == SKILL_AURA_BIRD_OWL) && (player.getX() > -166168 || player.getZ() <= 0 || player.getZ() >= 6000 || player.getPet() != null || player.getReflection() != ReflectionManager.DEFAULT))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_id, _level));
			return false;
		}

		// Нельзя отменять летающую трансформу слишком высоко над землей
		if(player.isInFlyingTransform() && getId() == SKILL_TRANSFORM_DISPEL && Math.abs(player.getZ() - player.getLoc().correctGeoZ().z) > 333)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(_id, _level));
			return false;
		}

		if(player.isInWater())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			return false;
		}

		if(player.isRiding() || player.getMountType() == 2)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
			return false;
		}

		// Для трансформации у игрока не должно быть активировано умение Mystic Immunity.
		if(player.getEffectList().getEffectsBySkillId(Skill.SKILL_MYSTIC_IMMUNITY) != null)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_UNDER_THE_EFFECT_OF_A_SPECIAL_SKILL);
			return false;
		}

		if(player.isInBoat())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_BOAT);
			return false;
		}

		if(useSummon)
		{
			if(player.getPet() == null || !player.getPet().isSummon() || player.getPet().isDead())
			{
				activeChar.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
				return false;
			}
		}
		if(!isSummonerTransformation() && !useSummon)
		if(player.getPet() != null && getId() != SKILL_TRANSFORM_DISPEL && !isBaseTransformation() && !player.getPet().isPet())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITOR_PET);
			return false;
		}
		Zone QueenAntZone = ReflectionUtils.getZone("[queen_ant_epic]");
		if(player.isInZone(QueenAntZone) && getId() != SKILL_TRANSFORM_DISPEL && !isBaseTransformation() && !isSummonerTransformation())
		{
			player.sendMessage(player.isLangRus() ? "Здесь запрещено находиться в трансформации." : "It is forbidden to be in transformation.");
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(useSummon)
		{
			if(activeChar.getPet() == null || !activeChar.getPet().isSummon() || activeChar.getPet().isDead())
			{
				activeChar.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
				return;
			}
			activeChar.getPet().unSummon();
		}

		if(isSummonerTransformation() && activeChar.getPet() != null && activeChar.getPet().isSummon())
			activeChar.getPet().unSummon();

		for(Creature target : targets)
			if(target != null && target.isPlayer())
				getEffects(activeChar, target, false, false);

		if(isSSPossible())
			if(!(Config.ALT_SAVING_SPS && _skillType == SkillType.BUFF))
				activeChar.unChargeShots(isMagic());
	}
}