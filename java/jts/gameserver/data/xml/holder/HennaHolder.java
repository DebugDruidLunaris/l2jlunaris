package jts.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.iterator.TIntObjectIterator;

import java.util.ArrayList;
import java.util.List;

import jts.commons.data.xml.AbstractHolder;
import jts.gameserver.model.Player;
import jts.gameserver.templates.Henna;

public final class HennaHolder extends AbstractHolder
{
	private static final HennaHolder _instance = new HennaHolder();

	private TIntObjectHashMap<Henna> _hennas = new TIntObjectHashMap<Henna>();

	public static HennaHolder getInstance()
	{
		return _instance;
	}

	public void addHenna(Henna h)
	{
		_hennas.put(h.getSymbolId(), h);
	}

	public Henna getHenna(int symbolId)
	{
		return _hennas.get(symbolId);
	}

	public List<Henna> generateList(Player player)
	{
		List<Henna> list = new ArrayList<Henna>();
		for(TIntObjectIterator<Henna> iterator = _hennas.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			Henna h = iterator.value();
			if(h.isForThisClass(player))
				list.add(h);
		}

		return list;
	}

	@Override
	public int size()
	{
		return _hennas.size();
	}

	@Override
	public void clear()
	{
		_hennas.clear();
	}
}