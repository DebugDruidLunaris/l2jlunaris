package jts.gameserver.model;

public class ClanListObject
{
	int obj_id;
	int list_status;
	int item_id;
	int price;
	
	public ClanListObject(int _obj_id, int _list_status, int _item_id, int _price)
	{
		obj_id = _obj_id;
		list_status = _list_status;
		item_id = _item_id;
		price = _price;
	}
	
	public void set_obj_id(int _obj_id)
	{
		obj_id = _obj_id;
	}
	
	public void set_list_status(int _list_status)
	{
		list_status = _list_status;
	}
	
	public void set_item_id(int _item_id)
	{
		item_id = _item_id;
	}
	
	public void set_price(int _price)
	{
		price = _price;
	}
	
	public int get_obj_id()
	{
		return obj_id;
	}
	
	public int get_list_status()
	{
		return list_status;
	}
	
	public int get_item_id()
	{
		return item_id;
	}
	
	public int get_price()
	{
		return price;
	}
}