package jts.gameserver.model;

import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.templates.item.ItemTemplate;
import jts.gameserver.utils.Location;

public class TeleportLocation extends Location
{
	private static final long serialVersionUID = 1L;
	private final long _price;
	private final ItemTemplate _item;
	private final int _name;
	private final int _castleId;
	private final String _RuName;
	private final String _EnName;

	public TeleportLocation(int item, long price, int name, String RuName, String EnName, int castleId)
	{
		_price = price;
		_name = name;
		_RuName = RuName;
		_EnName = EnName;
		_item = ItemHolder.getInstance().getTemplate(item);
		_castleId = castleId;
	}

	public long getPrice()
	{
		return _price;
	}

	public ItemTemplate getItem()
	{
		return _item;
	}

	public int getName()
	{
		return _name;
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public String RuName()
	{
		return _RuName;
	}

	public String EnName()
	{
		return _EnName;
	}
}