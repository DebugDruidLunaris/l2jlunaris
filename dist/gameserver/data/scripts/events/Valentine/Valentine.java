package events.Valentine;

import java.util.ArrayList;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Valentine extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static int EVENT_MANAGER_ID = 4301;
	private static final Logger _log = LoggerFactory.getLogger(Valentine.class);

	private static int[][] _dropdata = 
	{
		// Item, chance
		{ 20192, 10 },
		{ 20193, 10 },
		{ 20194, 5 },

	};

	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	private static boolean _active = false;

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Valentine [state: activated]");
		}
		else
			_log.info("Loaded Event: Valentine [state: deactivated]");
	}

	/**
	 * Читает статус эвента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("Valentine");
	}

	/**
	 * Запускает эвент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("Valentine", true))
		{
			spawnEventManagers();
			System.out.println("Event 'Valentine' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Valentine.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Valentine' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает эвент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("Valentine", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'Valentine' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Valentine.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Valentine' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	/**
	 * Спавнит эвент менеджеров
	 */
	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = {
				{ 81921, 148921, -3467, 16384 },
				{ 146405, 28360, -2269, 49648 },
				{ 19319, 144919, -3103, 31135 },
				{ -82805, 149890, -3129, 16384 },
				{ -12347, 122549, -3104, 16384 },
				{ 110642, 220165, -3655, 61898 },
				{ 116619, 75463, -2721, 20881 },
				{ 85513, 16014, -3668, 23681 },
				{ 81999, 53793, -1496, 61621 },
				{ 148159, -55484, -2734, 44315 },
				{ 44185, -48502, -797, 27479 },
				{ 86899, -143229, -1293, 8192 } };


		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	/**
	 * Удаляет спавн эвент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}

	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer))
		{
			int dropCounter = 0;
			for(int[] drop : _dropdata)
				if(Rnd.chance(drop[1] * killer.getPlayer().getRateItems() * Config.RATE_DROP_ITEMS * 0.1))
				{
					dropCounter++;
					((NpcInstance) cha).dropItem(killer.getPlayer(), drop[0], 1);

					// Из одного моба выпадет не более 3-х эвентовых предметов
					if(dropCounter > 2)
						break;
				}
		}
	}


	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Valentine.AnnounceEventStarted", null);
	}
}