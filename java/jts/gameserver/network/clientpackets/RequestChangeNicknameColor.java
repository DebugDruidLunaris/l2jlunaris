package jts.gameserver.network.clientpackets;

import jts.gameserver.model.Player;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.utils.Log_New;

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int[] COLORS = { 9671679, 8145404, 9959676, 16423662, 16735635, 64672, 10528257, 7903407, 4743829, 10066329 };
	private int _colorNum;
	private int _itemObjectId;
	private String _title;

	@Override
	protected void readImpl()
	{
		_colorNum = readD();
		_title = readS();
		_itemObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_colorNum < 0 || _colorNum >= COLORS.length)
			return;

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(item == null)
			return;

		if(activeChar.consumeItem(item.getItemId(), 1L))
		{
			activeChar.setTitleColor(COLORS[_colorNum]);
			activeChar.setTitle(_title);
			activeChar.broadcastUserInfo(true);
			Log_New.LogEvent(activeChar.getName(), "ChangeName", "NickChange", new String[] { "changed color to: " + COLORS[_colorNum] + "" });
		}
	}
}