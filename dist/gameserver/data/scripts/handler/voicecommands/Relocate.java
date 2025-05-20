package handler.voicecommands;

import java.util.List;

import jts.gameserver.cache.Msg;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.handler.voicecommands.VoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.SystemMessage;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.skills.skillclasses.Call;
import jts.gameserver.utils.Location;

public class Relocate implements IVoicedCommandHandler, ScriptFile
{
	public static int SUMMON_PRICE = 5;

	private final String[] _commandList = new String[] { "summon_clan" };

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(command.equalsIgnoreCase("summon_clan"))
		{
			if(!activeChar.isClanLeader())
			{
				activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return false;
			}

			SystemMessage msg = Call.canSummonHere(activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}

			if(activeChar.isAlikeDead())
			{
				activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Relocate.Dead", activeChar));
				return false;
			}

			List<Player> clan = activeChar.getClan().getOnlineMembers(activeChar.getObjectId());

			for(Player pl : clan)
				if(Call.canBeSummoned(pl) == null)
					// Peça autorização para recorrer
					pl.summonCharacterRequest(activeChar, Location.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()), SUMMON_PRICE);

			return true;
		}
		return false;
	}

	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}
}