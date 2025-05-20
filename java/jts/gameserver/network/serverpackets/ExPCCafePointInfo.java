package jts.gameserver.network.serverpackets;

import jts.gameserver.model.Player;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private int _mAddPoint, _mPeriodType, _pointType, _pcBangPoints, _remainTime;

	public ExPCCafePointInfo(Player player, int mAddPoint, int mPeriodType, int pointType, int remainTime)
	{
		_pcBangPoints = player.getPcBangPoints();
		_mAddPoint = mAddPoint;
		_mPeriodType = mPeriodType;
		_pointType = pointType;
		_remainTime = remainTime;
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x32);
		writeD(_pcBangPoints);
		writeD(_mAddPoint);
		writeC(_mPeriodType);
		writeD(_remainTime);
		writeC(_pointType);
	}
}