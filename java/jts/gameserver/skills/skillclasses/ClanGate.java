package jts.gameserver.skills.skillclasses;

import java.util.List;

import jts.gameserver.cache.Msg;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.templates.StatsSet;

public class ClanGate extends Skill
{
	public ClanGate(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		Player player = (Player) activeChar;
		if(!player.isClanLeader())
		{
			player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
			return false;
		}

		SystemMessage msg = Call.canSummonHere(player);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
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
		Clan clan = player.getClan();
		clan.broadcastToOtherOnlineMembers(Msg.COURT_MAGICIAN__THE_PORTAL_HAS_BEEN_CREATED, player);

		getEffects(activeChar, activeChar, getActivateRate() > 0, true);
	}
}