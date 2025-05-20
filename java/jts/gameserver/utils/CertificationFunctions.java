package jts.gameserver.utils;

import java.util.Collection;

import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.SkillAcquireHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.SkillLearn;
import jts.gameserver.model.SubClass;
import jts.gameserver.model.base.AcquireType;
import jts.gameserver.model.base.ClassId;
import jts.gameserver.model.base.ClassType2;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.SkillList;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

public class CertificationFunctions
{
	public static final String PATH = "villagemaster/certification/";

	public static void showCertificationList(NpcInstance npc, Player player)
	{
		if(!checkConditions(65, npc, player, true))
			return;

		Functions.show(PATH + "certificatelist.htm", player, npc);
	}

	public static void getCertification65(NpcInstance npc, Player player)
	{
		if(!checkConditions(65, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();
		if(clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_65);
		player.store(true);
	}

	public static void getCertification70(NpcInstance npc, Player player)
	{
		if(!checkConditions(70, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if(!clzz.isCertificationGet(SubClass.CERTIFICATION_65))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if(clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.addItem(player, 10280, 1);
		clzz.addCertification(SubClass.CERTIFICATION_70);
		player.store(true);
	}

	public static void getCertification75List(NpcInstance npc, Player player)
	{
		if(!checkConditions(75, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if(!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if(clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		Functions.show(PATH + "certificate-choose.htm", player, npc);
	}

	public static void getCertification75(NpcInstance npc, Player player, boolean classCertifi)
	{
		if(!checkConditions(75, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят преведущий сертификат
		if(!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if(clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		if(classCertifi)
		{
			ClassId cl = ClassId.VALUES[clzz.getClassId()];
			if(cl.getType2() == null)
				return;

			Functions.addItem(player, cl.getType2().getCertificateId(), 1);
		}
		else
			Functions.addItem(player, 10612, 1); // master ability

		clzz.addCertification(SubClass.CERTIFICATION_75);
		player.store(true);
	}

	public static void getCertification80(NpcInstance npc, Player player)
	{
		if(!checkConditions(80, npc, player, false))
			return;

		SubClass clzz = player.getActiveClass();

		// если не взят(ы) преведущий сертификат(ы)
		if(!clzz.isCertificationGet(SubClass.CERTIFICATION_65) || !clzz.isCertificationGet(SubClass.CERTIFICATION_70) || !clzz.isCertificationGet(SubClass.CERTIFICATION_75))
		{
			Functions.show(PATH + "certificate-fail.htm", player, npc);
			return;
		}

		if(clzz.isCertificationGet(SubClass.CERTIFICATION_80))
		{
			Functions.show(PATH + "certificate-already.htm", player, npc);
			return;
		}

		ClassId cl = ClassId.VALUES[clzz.getClassId()];
		if(cl.getType2() == null)
			return;

		Functions.addItem(player, cl.getType2().getTransformationId(), 1);
		clzz.addCertification(SubClass.CERTIFICATION_80);
		player.store(true);
	}

	public static void cancelCertification(NpcInstance npc, Player player)
	{
		if(player.getInventory().getAdena() < 10000000)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(!player.getActiveClass().isBase())
			return;

		player.getInventory().reduceAdena(10000000);

		for(ClassType2 classType2 : ClassType2.VALUES)
		{
			player.getInventory().destroyItemByItemId(classType2.getCertificateId(), player.getInventory().getCountOf(classType2.getCertificateId()));
			player.getInventory().destroyItemByItemId(classType2.getTransformationId(), player.getInventory().getCountOf(classType2.getTransformationId()));
		}

		Collection<SkillLearn> skillLearnList = SkillAcquireHolder.getInstance().getAvailableSkills(null, AcquireType.CERTIFICATION);
		for(SkillLearn learn : skillLearnList)
		{
			Skill skill = player.getKnownSkill(learn.getId());
			if(skill != null)
				player.removeSkill(skill, true);
		}

		for(SubClass subClass : player.getSubClasses().values())
			if(!subClass.isBase())
				subClass.setCertification(0);

		player.sendPacket(new SkillList(player));
		Functions.show(new CustomMessage("scripts.services.SubclassSkills.SkillsDeleted", player), player);
	}

	public static boolean checkConditions(int level, NpcInstance npc, Player player, boolean first)
	{
		if(player.getLevel() < level)
		{
			Functions.show(PATH + "certificate-nolevel.htm", player, npc, "%level%", level);
			return false;
		}

		if(player.getActiveClass().isBase())
		{
			Functions.show(PATH + "certificate-nosub.htm", player, npc);
			return false;
		}
		return true;
	}
}