package npc.model.events;

import java.util.List;

import jts.commons.collections.CollectionUtils;
import jts.gameserver.data.xml.holder.EventHolder;
import jts.gameserver.model.Party;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.EventType;
import jts.gameserver.model.entity.events.impl.UndergroundColiseumEvent;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.templates.npc.NpcTemplate;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public class ColiseumManagerInstance extends ColiseumHelperInstance
{
	private String _startHtm;
	private int _coliseumId;

	public ColiseumManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		_startHtm = getParameter("start_htm", StringUtils.EMPTY);
		_coliseumId = getParameter("coliseum_id", 0);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		UndergroundColiseumEvent coliseumEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, _coliseumId);

		if(command.equals("register"))
		{
			Party party = player.getParty();
			if(party == null)
				showChatWindow(player, "events/kerthang_manager008.htm");
			else if(party.getPartyLeader() != player)
				showChatWindow(player, "events/kerthang_manager004.htm");
			else
				for(Player $player : party)
					if($player.getLevel() < coliseumEvent.getMinLevel() || $player.getLevel() > coliseumEvent.getMaxLevel())
					{
						showChatWindow(player, "events/kerthang_manager011.htm", "%name%", $player.getName());
						return;
					}
		}
		else if(command.equals("viewTeams"))
		{

			List<Player> reg = coliseumEvent.getRegisteredPlayers();

			NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("events/kerthang_manager003.htm");
			for(int i = 0; i < 5; i++)
			{
				Player $player = CollectionUtils.safeGet(reg, i);

				msg.replace("%team" + i + "%", $player == null ? StringUtils.EMPTY : $player.getName());
			}

			player.sendPacket(msg);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... ar)
	{
		showChatWindow(player, _startHtm);
	}
}