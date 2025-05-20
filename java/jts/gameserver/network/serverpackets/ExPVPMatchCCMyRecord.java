package jts.gameserver.network.serverpackets;

import jts.gameserver.model.entity.events.objects.KrateisCubePlayerObject;

public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
	private int _points;

	public ExPVPMatchCCMyRecord(KrateisCubePlayerObject player)
	{
		_points = player.getPoints();
	}

	@Override
	public void writeImpl()
	{
		writeEx(0x8A);
		writeD(_points);
	}
}