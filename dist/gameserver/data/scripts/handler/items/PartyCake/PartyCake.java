package handler.items.PartyCake;

import handler.items.ScriptItemHandler;
import jts.commons.threading.RunnableImpl;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.World;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.network.serverpackets.SystemMessage2;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.templates.npc.NpcTemplate;

public class PartyCake extends ScriptItemHandler
{
	public class DeSpawnScheduleTimerTask extends RunnableImpl
	{
		SimpleSpawner spawnedTree = null;

		public DeSpawnScheduleTimerTask(SimpleSpawner spawn)
		{
			spawnedTree = spawn;
		}

		@Override
		public void runImpl() throws Exception
		{
			spawnedTree.deleteAll();
		}
	}

	private static int[] _itemIds = { 21881 };

	private static final int DESPAWN_TIME = 600000; //10 min

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player activeChar = (Player) playable;
		NpcTemplate template = null;

		int itemId = item.getItemId();
		for(int i = 0; i < _itemIds.length; i++)
			if(_itemIds[i] == itemId)
			{
				template = NpcHolder.getInstance().getTemplate(147);
				break;
			}

		for(NpcInstance npc : World.getAroundNpc(activeChar, 300, 200))
			if(npc.getNpcId() == 147)
			{
				activeChar.sendPacket(new SystemMessage2(SystemMsg.SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN).addName(npc));
				return false;
			}

		// A ban on cakes Sammon slischkom close to other NPCs
		if(World.getAroundNpc(activeChar, 100, 200).size() > 0)
		{
			activeChar.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}

		if(template == null)
			return false;

		if(!activeChar.getInventory().destroyItem(item, 1L))
			return false;

		SimpleSpawner spawn = new SimpleSpawner(template);
		spawn.setLoc(activeChar.getLoc());
		NpcInstance npc = spawn.doSpawn(false);
		npc.setTitle(activeChar.getName()); //FIXME Por que não está instalado
		spawn.respawnNpc(npc);

		npc.setAI(new AI(npc));

		ThreadPoolManager.getInstance().schedule(new DeSpawnScheduleTimerTask(spawn), activeChar.isInPeaceZone() ? DESPAWN_TIME / 3 : DESPAWN_TIME);
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}