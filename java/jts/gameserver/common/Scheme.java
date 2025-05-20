package jts.gameserver.common;

import gnu.trove.map.hash.THashMap;

public class Scheme
{
	private String name;
	private THashMap<Integer, Integer> buffs = new THashMap<Integer, Integer>();

	public Scheme(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void addBuff(int id, int level)
	{
		buffs.put(Integer.valueOf(id), Integer.valueOf(level));
	}

	public THashMap<Integer, Integer> getBuffs()
	{
		return buffs;
	}
}