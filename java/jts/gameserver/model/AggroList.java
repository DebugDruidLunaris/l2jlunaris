package jts.gameserver.model;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jts.commons.collections.LazyArrayList;
import jts.commons.util.Rnd;
import jts.gameserver.model.instances.NpcInstance;

public class AggroList
{
	private abstract class DamageHate
	{
		public int hate;
		public int damage;
	}

	public class HateInfo extends DamageHate
	{
		public final Creature attacker;

		HateInfo(Creature attacker, AggroInfo ai)
		{
			this.attacker = attacker;
			hate = ai.hate;
			damage = ai.damage;
		}
	}

	public class AggroInfo extends DamageHate
	{
		public final int attackerId;

		AggroInfo(Creature attacker)
		{
			attackerId = attacker.getObjectId();
		}
	}

	public static class DamageComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance = new DamageComparator();

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		DamageComparator() {}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			return o2.damage - o1.damage;
		}
	}

	public static class HateComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance = new HateComparator();

		public static Comparator<DamageHate> getInstance()
		{
			return instance;
		}

		HateComparator() {}

		@Override
		public int compare(DamageHate o1, DamageHate o2)
		{
			int diff = o2.hate - o1.hate;
			return diff == 0 ? o2.damage - o1.damage : diff;
		}
	}

	private final NpcInstance npc;
	private final TIntObjectHashMap<AggroInfo> hateList = new TIntObjectHashMap<AggroInfo>();
	/** Блокировка для чтения/записи объектов списка */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	public AggroList(NpcInstance npc)
	{
		this.npc = npc;
	}

	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		damage = Math.max(damage, 0);

		if(damage == 0 && aggro == 0)
			return;

		writeLock.lock();
		try
		{
			AggroInfo ai;

			if((ai = hateList.get(attacker.getObjectId())) == null)
				hateList.put(attacker.getObjectId(), ai = new AggroInfo(attacker));

			ai.damage += damage;
			ai.hate += aggro;
			ai.damage = Math.max(ai.damage, 0);
			ai.hate = Math.max(ai.hate, 0);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public AggroInfo get(Creature attacker)
	{
		readLock.lock();
		try
		{
			return hateList.get(attacker.getObjectId());
		}
		finally
		{
			readLock.unlock();
		}
	}

	public void remove(Creature attacker, boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(!onlyHate)
			{
				hateList.remove(attacker.getObjectId());
				return;
			}

			AggroInfo ai = hateList.get(attacker.getObjectId());
			if(ai != null)
				ai.hate = 0;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void clear()
	{
		clear(false);
	}

	public void clear(boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(hateList.isEmpty())
				return;

			if(!onlyHate)
			{
				hateList.clear();
				return;
			}

			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				ai.hate = 0;
				if(ai.damage == 0)
					itr.remove();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean isEmpty()
	{
		readLock.lock();
		try
		{
			return hateList.isEmpty();
		}
		finally
		{
			readLock.unlock();
		}
	}

	public List<Creature> getHateList()
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{

			if(hateList.isEmpty())
				return Collections.emptyList();

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return Collections.emptyList();

		List<Creature> hateList = new LazyArrayList<Creature>();
		List<Creature> chars = World.getAroundCharacters(npc);
		AggroInfo ai;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
				if(cha.getObjectId() == ai.attackerId)
				{
					hateList.add(cha);
					break;
				}
		}

		return hateList;
	}

	public Creature getMostHated()
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{

			if(hateList.isEmpty())
				return null;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;

		List<Creature> chars = World.getAroundCharacters(npc);

		AggroInfo ai;
		loop: for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
				if(cha.getObjectId() == ai.attackerId)
				{
					if(cha.isDead())
						continue loop;
					return cha;
				}
		}

		return null;
	}

	public Creature getRandomHated()
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return null;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;

		List<Creature> chars = World.getAroundCharacters(npc);

		LazyArrayList<Creature> randomHated = LazyArrayList.newInstance();

		AggroInfo ai;
		Creature mostHated;
		loop: for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.hate == 0)
				continue;
			for(Creature cha : chars)
				if(cha.getObjectId() == ai.attackerId)
				{
					if(cha.isDead())
						continue loop;
					randomHated.add(cha);
					break;
				}
		}

		if(randomHated.isEmpty())
			mostHated = null;
		else
			mostHated = randomHated.get(Rnd.get(randomHated.size()));

		LazyArrayList.recycle(randomHated);

		return mostHated;
	}

	public Creature getTopDamager()
	{
		AggroInfo[] hated;

		readLock.lock();
		try
		{
			if(hateList.isEmpty())
				return null;

			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}

		Creature topDamager = null;
		Arrays.sort(hated, DamageComparator.getInstance());
		if(hated[0].damage == 0)
			return null;

		List<Creature> chars = World.getAroundCharacters(npc);
		AggroInfo ai;
		for(int i = 0; i < hated.length; i++)
		{
			ai = hated[i];
			if(ai.damage == 0)
				continue;
			for(Creature cha : chars)
				if(cha.getObjectId() == ai.attackerId)
				{
					topDamager = cha;
					return topDamager;
				}
		}
		return null;
	}

	public Map<Creature, HateInfo> getCharMap()
	{
		if(isEmpty())
			return Collections.emptyMap();

		Map<Creature, HateInfo> aggroMap = new HashMap<Creature, HateInfo>();
		List<Creature> chars = World.getAroundCharacters(npc);
		readLock.lock();
		try
		{
			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Creature attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}

		return aggroMap;
	}

	public Map<Playable, HateInfo> getPlayableMap()
	{
		if(isEmpty())
			return Collections.emptyMap();

		Map<Playable, HateInfo> aggroMap = new HashMap<Playable, HateInfo>();
		List<Playable> chars = World.getAroundPlayables(npc);
		readLock.lock();
		try
		{
			AggroInfo ai;
			for(TIntObjectIterator<AggroInfo> itr = hateList.iterator(); itr.hasNext();)
			{
				itr.advance();
				ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(Playable attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}
}