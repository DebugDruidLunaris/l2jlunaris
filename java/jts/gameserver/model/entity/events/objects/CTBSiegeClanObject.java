package jts.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.dao.SiegePlayerDAO;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.events.impl.SiegeEvent;
import jts.gameserver.model.entity.residence.Residence;
import jts.gameserver.model.pledge.Clan;

@SuppressWarnings("serial")
public class CTBSiegeClanObject extends SiegeClanObject
{
	private List<Integer> _players = new ArrayList<Integer>();
	private long _npcId;

	public CTBSiegeClanObject(String type, Clan clan, long param, long date)
	{
		super(type, clan, param, date);
		_npcId = param;
	}

	public CTBSiegeClanObject(String type, Clan clan, long param)
	{
		this(type, clan, param, System.currentTimeMillis());
	}

	public void select(Residence r)
	{
		_players.addAll(SiegePlayerDAO.getInstance().select(r, getObjectId()));
	}

	public List<Integer> getPlayers()
	{
		return _players;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setEvent(boolean start, SiegeEvent event)
	{
		for(int i : getPlayers())
		{
			Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
			{
				if(start)
					player.addEvent(event);
				else
					player.removeEvent(event);
				player.broadcastCharInfo();
			}
		}
	}

	@Override
	public boolean isParticle(Player player)
	{
		return _players.contains(player.getObjectId());
	}

	@Override
	public long getParam()
	{
		return _npcId;
	}

	public void setParam(int npcId)
	{
		_npcId = npcId;
	}
}