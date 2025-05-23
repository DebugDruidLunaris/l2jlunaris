package jts.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jts.gameserver.data.xml.holder.ResidenceHolder;
import jts.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import jts.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import jts.gameserver.model.entity.residence.ClanHall;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.tables.ClanTable;

import org.apache.commons.lang3.StringUtils;

public class ExShowAgitInfo extends L2GameServerPacket
{
	private List<AgitInfo> _clanHalls = Collections.emptyList();

	public ExShowAgitInfo()
	{
		List<ClanHall> chs = ResidenceHolder.getInstance().getResidenceList(ClanHall.class);
		_clanHalls = new ArrayList<AgitInfo>(chs.size());

		for(ClanHall clanHall : chs)
		{
			int ch_id = clanHall.getId();
			int getType;
			if(clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class)
				getType = 0;
			else if(clanHall.getSiegeEvent().getClass() == ClanHallMiniGameEvent.class)
				getType = 2;
			else
				getType = 1;

			Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
			String clan_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getName();
			String leader_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getLeaderName();
			_clanHalls.add(new AgitInfo(clan_name, leader_name, ch_id, getType));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x16);
		writeD(_clanHalls.size());
		for(AgitInfo info : _clanHalls)
		{
			writeD(info.ch_id);
			writeS(info.clan_name);
			writeS(info.leader_name);
			writeD(info.getType);
		}
	}

	static class AgitInfo
	{
		public String clan_name, leader_name;
		public int ch_id, getType;

		public AgitInfo(String clan_name, String leader_name, int ch_id, int lease)
		{
			this.clan_name = clan_name;
			this.leader_name = leader_name;
			this.ch_id = ch_id;
			getType = lease;
		}
	}
}