package jts.gameserver.utils;

import jts.commons.util.Rnd;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.instancemanager.ReflectionManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectTasks;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.Reflection;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.clientpackets.L2GameClientPacket;
import jts.gameserver.templates.npc.NpcTemplate;

public class NpcUtils
{
	public static NpcInstance canPassPacket(Player player, L2GameClientPacket packet, Object... arg)
	{
		final NpcInstance npcInstance = player.getLastNpc();
		return (npcInstance != null && player.isInRangeZ(npcInstance.getLoc(), Creature.INTERACTION_DISTANCE) && npcInstance.canPassPacket(player, packet.getClass(), arg)) ? npcInstance : null;
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1), ReflectionManager.DEFAULT, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, int x, int y, int z, int h, long despawnTime)
	{
		return spawnSingle(npcId, new Location(x, y, z, h), ReflectionManager.DEFAULT, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection)
	{
		return spawnSingle(npcId, loc, reflection, 0, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime)
	{
		return spawnSingle(npcId, loc, reflection, despawnTime, null);
	}

	public static NpcInstance spawnSingle(int npcId, Location loc, Reflection reflection, long despawnTime, String title)
	{
		NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
		if(template == null)
			throw new NullPointerException("Npc template id : " + npcId + " not found!");

		NpcInstance npc = template.getNewInstance();
		npc.setHeading(loc.h < 0 ? Rnd.get(0xFFFF) : loc.h);
		npc.setSpawnedLoc(loc);
		npc.setReflection(reflection);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
		if(title != null)
			npc.setTitle(title);

		npc.spawnMe(npc.getSpawnedLoc());
		if(despawnTime > 0)
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(npc), despawnTime);
		return npc;
	}
}
