package jts.gameserver.network.clientpackets;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.network.serverpackets.NickNameChanged;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.utils.Util;

public class RequestGiveNickName extends L2GameClientPacket
{
	private String _target;
	private String _title;

	@Override
	protected void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
		_title = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!_title.isEmpty() && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			activeChar.sendMessage("Incorrect title.");
			return;
		}

		// Дворяне могут устанавливать/менять себе title
		if(activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(Msg.TITLE_HAS_CHANGED);
			activeChar.broadcastPacket(new NickNameChanged(activeChar));
			return;
		}
		// Can the player change/give a title?
		else if((activeChar.getClanPrivileges() & Clan.CP_CL_MANAGE_TITLES) != Clan.CP_CL_MANAGE_TITLES)
			return;

		if(activeChar.getClan().getLevel() < 3)
		{
			activeChar.sendPacket(Msg.TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
			return;
		}

		UnitMember member = activeChar.getClan().getAnyMember(_target);
		if(member != null)
		{
			member.setTitle(_title);
			if(member.isOnline())
			{
				member.getPlayer().sendPacket(Msg.TITLE_HAS_CHANGED);
				member.getPlayer().sendChanges();
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("jts.gameserver.network.clientpackets.RequestGiveNickName.NotInClan", activeChar));
	}
}