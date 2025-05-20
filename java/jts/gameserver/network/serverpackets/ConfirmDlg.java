package jts.gameserver.network.serverpackets;

import jts.gameserver.network.serverpackets.components.SystemMsg;

public class ConfirmDlg extends SysMsgContainer<ConfirmDlg>
{
	private int _time;
	private int _requestId;

	public ConfirmDlg(SystemMsg msg, int time)
	{
		super(msg);
		_time = time;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf3);
		writeElements();
		writeD(_time);
		writeD(_requestId);
	}

	public void setRequestId(int requestId)
	{
		_requestId = requestId;
	}
}