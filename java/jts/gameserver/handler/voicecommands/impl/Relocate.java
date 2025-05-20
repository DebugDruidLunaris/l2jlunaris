package jts.gameserver.handler.voicecommands.impl;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.skills.skillclasses.Call;
import jts.gameserver.utils.Location;

/**
 * @author Gremory 11.08.2015 03:18
 */
public class Relocate
  implements IVoicedCommandHandler
{
  private String[] _commandList = { "km-all-to-me" };

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
	if (!Config.ENABLE_KM_ALL_TO_ME) 
	{
	  return false;
	}
	if (!activeChar.isClanLeader())
	{
	  activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
	  return false;
	}

	SystemMessage msg = Call.canSummonHere(activeChar);
	if (msg != null)
	{
	  activeChar.sendPacket(msg);
	  return false;
	}

	for (Player player : activeChar.getClan().getOnlineMembers(activeChar.getObjectId())) {
	  if (Call.canBeSummoned(player) == null)
		player.summonCharacterRequest(activeChar, Location.findAroundPosition(activeChar, 100, 150), 5);
	}
	return true;
  }

  public String[] getVoicedCommandList()
  {
	return this._commandList;
  }
}
