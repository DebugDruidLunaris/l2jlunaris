package jts.gameserver.model.entity.events.actions;

import java.util.List;

import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.EventAction;
import jts.gameserver.model.entity.events.GlobalEvent;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.network.serverpackets.components.SysString;
import jts.gameserver.network.serverpackets.components.SystemMsg;

public class SayAction implements EventAction
{
	private int _range;
	private ChatType _chatType;

	private String _how;
	private NpcString _text;

	private SysString _sysString;
	private SystemMsg _systemMsg;

	protected SayAction(int range, ChatType type)
	{
		_range = range;
		_chatType = type;
	}

	public SayAction(int range, ChatType type, SysString sysString, SystemMsg systemMsg)
	{
		this(range, type);
		_sysString = sysString;
		_systemMsg = systemMsg;
	}

	public SayAction(int range, ChatType type, String how, NpcString string)
	{
		this(range, type);
		_text = string;
		_how = how;
	}

	@Override
	public void call(GlobalEvent event)
	{
		List<Player> players = event.broadcastPlayers(_range);
		for(Player player : players)
			packet(player);
	}

	private void packet(Player player)
	{
		if(player == null)
			return;

		L2GameServerPacket packet = null;
		if(_sysString != null)
			packet = new Say2(0, _chatType, _sysString, _systemMsg);
		else
			packet = new Say2(0, _chatType, _how, _text);

		player.sendPacket(packet);
	}
}