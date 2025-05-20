package jts.gameserver.network.serverpackets;

import jts.gameserver.model.entity.events.impl.KrateisCubeEvent;
import jts.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

public class ExPVPMatchCCRecord extends L2GameServerPacket
{
	private final KrateisCubePlayerObject[] _players;

	public ExPVPMatchCCRecord(KrateisCubeEvent cube)
	{
		_players = cube.getSortedPlayers();
	}

	@Override
	public void writeImpl()
	{
		writeEx(0x89);
		writeD(0x00); // Open/Dont Open
		writeD(_players.length);
		for(KrateisCubePlayerObject p : _players)
		{
			writeS(p.getName());
			writeD(p.getPoints());
		}
	}
}