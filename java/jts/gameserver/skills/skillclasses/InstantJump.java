package jts.gameserver.skills.skillclasses;

import jts.commons.util.Rnd;
import jts.gameserver.Config;
import jts.gameserver.ai.CtrlEvent;
import jts.gameserver.ai.CtrlIntention;
import jts.gameserver.geodata.GeoEngine;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Skill;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.FlyToLocation;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.ValidateLocation;
import jts.gameserver.stats.Stats;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;

import java.util.List;

public class InstantJump extends Skill 
{

	public InstantJump(StatsSet set) 
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets) 
	{
		if (targets.size() == 0)
			return;

		Creature target = targets.get(0);
		if (Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0, activeChar, this))) {
			if (activeChar.isPlayer())
				activeChar.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(target));
			if (target.isPlayer())
				target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(activeChar));
			return;
		}
		int x, y, z;

		int px = target.getX();
		int py = target.getY();
		double ph = PositionUtils.convertHeadingToDegree(target.getHeading());

		ph += 180;

		if (ph > 360)
			ph -= 360;

		ph = (Math.PI * ph) / 180;

		x = (int) (px + (25 * Math.cos(ph)));
		y = (int) (py + (25 * Math.sin(ph)));
		z = target.getZ();

		Location loc = new Location(x, y, z);

		if (Config.ALLOW_GEODATA)
			loc = GeoEngine.moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, activeChar.getReflection().getGeoIndex());

		if (target.isNpc()) {
			NpcInstance npc = (NpcInstance) target;
			npc.abortAttack(true, true);
			npc.abortCast(true, true);
			npc.stopMove();
			npc.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		} else
			target.setTarget(null);
			target.stopMove();
			target.abortAttack(true, true);
			target.abortCast(true, true);

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc, FlyToLocation.FlyType.DUMMY));
		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.setXYZ(loc.x, loc.y, loc.z);
		activeChar.setHeading(target.getHeading());
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
	}

}