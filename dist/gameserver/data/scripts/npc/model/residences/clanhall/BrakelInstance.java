package npc.model.residences.clanhall;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.templates.npc.NpcTemplate;
import jts.gameserver.utils.TimeUtils;

@SuppressWarnings("serial")
public class BrakelInstance extends NpcInstance
{
	public BrakelInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		ClanHall clanhall = ResidenceHolder.getInstance().getResidence(ClanHall.class, 21);
		if(clanhall == null)
			return;
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("residence2/clanhall/partisan_ordery_brakel001.htm");
		html.replace("%next_siege%", TimeUtils.toSimpleFormat(clanhall.getSiegeDate().getTimeInMillis()));
		player.sendPacket(html);
	}
}