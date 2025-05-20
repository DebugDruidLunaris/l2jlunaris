package jts.gameserver.model.entity.events.objects;

@SuppressWarnings("serial")
public class CastleDamageZoneObject extends ZoneObject
{
	private final long _price;

	public CastleDamageZoneObject(String name, long price)
	{
		super(name);
		_price = price;
	}

	public long getPrice()
	{
		return _price;
	}
}