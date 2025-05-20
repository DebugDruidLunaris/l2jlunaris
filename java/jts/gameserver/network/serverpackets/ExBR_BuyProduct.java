package jts.gameserver.network.serverpackets;

import jts.gameserver.Config;

public class ExBR_BuyProduct extends L2GameServerPacket
{
	public static final int RESULT_OK = 1; // ok
	public static final int RESULT_NOT_ENOUGH_POINTS = -1;
	public static final int RESULT_WRONG_PRODUCT = -2; // also -5
	public static final int RESULT_USER_CANCEL = -3;
	public static final int RESULT_INVENTORY_FULL = -4;
	public static final int RESULT_CLOSED_PRODUCT = -5;
	public static final int RESULT_SERVER_ERROR = -6;
	public static final int RESULT_BUY_BEFORE_SALE_DATE = -7;
	public static final int RESULT_BUY_AFTER_SALE_DATE = -8;
	public static final int RESULT_WRONG_USER_STATE = -9; // also -11
	public static final int RESULT_WRONG_PRODUCT_ITEM = -10;
	public static final int RESULT_BUY_NOT_DAY_OF_WEEK = -12;
	public static final int RESULT_BUY_NOT_TIME_OF_DAY = -13;
	public static final int RESULT_BUY_SOLD_OUT = -14;
	public static final int MAX_BUY_COUNT = Config.OTHER_ITEM_MALL_MAX_BUY_COUNT;

	private final int _result;

	public ExBR_BuyProduct(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xD8);

		writeD(_result);
	}
}