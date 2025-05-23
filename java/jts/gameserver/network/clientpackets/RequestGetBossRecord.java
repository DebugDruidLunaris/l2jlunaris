package jts.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jts.gameserver.instancemanager.RaidBossSpawnManager;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExGetBossRecord;
import jts.gameserver.network.serverpackets.ExGetBossRecord.BossRecordInfo;

public class RequestGetBossRecord extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _bossID;

	@Override
	protected void readImpl()
	{
		_bossID = readD(); // always 0?
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		int totalPoints = 0;
		int ranking = 0;

		if(activeChar == null)
			return;

		List<BossRecordInfo> list = new ArrayList<BossRecordInfo>();
		Map<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
		if(points != null && !points.isEmpty())
			for(Map.Entry<Integer, Integer> e : points.entrySet())
				switch(e.getKey())
				{
					case -1: // RaidBossSpawnManager.KEY_RANK
						ranking = e.getValue();
						break;
					case 0: //  RaidBossSpawnManager.KEY_TOTAL_POINTS
						totalPoints = e.getValue();
						break;
					default:
						list.add(new BossRecordInfo(e.getKey(), e.getValue(), 0));
				}

		activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
	}
}