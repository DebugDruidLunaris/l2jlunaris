package jts.gameserver.handler.admincommands.impl;

import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;

public class AdminPolymorph implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_polyself,
		admin_polymorph,
		admin_poly,
		admin_unpolyself,
		admin_unpolymorph,
		admin_unpoly
	}

	@Override
	@SuppressWarnings({ "fallthrough", "rawtypes" })
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanPolymorph)
			return false;

		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_polyself:
				target = activeChar;
			case admin_polymorph:
			case admin_poly:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				try
				{
					int id = Integer.parseInt(wordList[1]);
					if(NpcHolder.getInstance().getTemplate(id) != null)
					{
						((Player) target).setPolyId(id);
						((Player) target).broadcastCharInfo();
					}
					target.decayMe();
					target.spawnMe(target.getLoc());
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //poly id [type:npc|item]");
					return false;
				}
				break;
			case admin_unpolyself:
				target = activeChar;
			case admin_unpolymorph:
			case admin_unpoly:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return false;
				}
				((Player) target).setPolyId(0);
				((Player) target).broadcastCharInfo();
				break;
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}