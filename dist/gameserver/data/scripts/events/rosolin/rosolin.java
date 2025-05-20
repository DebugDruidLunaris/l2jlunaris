package events.rosolin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.network.serverpackets.ExBR_BroadcastEventState;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class rosolin extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static int EVENT_MANAGER = 4305; // Rosalin npc

	private static final Logger _log = LoggerFactory.getLogger(rosolin.class);

	// Rose item
	private static int EVENT_RED_ROSE = 20905;
	private static int EVENT_BLUEB_ROSE = 20906;
	private static int EVENT_WHITE_ROSE = 20907;


	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static boolean _active = false;
	private static boolean MultiSellLoaded = false;

	private static File[] multiSellFiles =
			{
			new File("data/xml/other/event/rosolin/502.xml"),
			};

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			loadMultiSell();
			spawnEventManagers();
			_log.info("Loaded Event: Au nom de la rose Event [state: activated]");
		}
		else
			_log.info("Loaded Event: Au nom de la rose Event [state: deactivated]");
	}

	/**
	 * Reads the status of the opening event of the base.
	 *
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("rosolin");
	}

	/**
	 * Starts EVENT
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("rosolin", true))
		{
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Event 'Au nom de la rose Event' started.");
			ExBR_BroadcastEventState es = new ExBR_BroadcastEventState(ExBR_BroadcastEventState.LOVERS_JUBILEE, 1);
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				p.sendPacket(es);
			Announcements.getInstance().announceByCustomMessage("scripts.events.rosolin.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Au nom de la rose Event' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	/**
	 * stops EVENT
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("rosolin", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'Au nom de la rose Event' stopped.");
			ExBR_BroadcastEventState es = new ExBR_BroadcastEventState(ExBR_BroadcastEventState.LOVERS_JUBILEE, 0);
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				p.sendPacket(es);
			Announcements.getInstance().announceByCustomMessage("scripts.events.rosolin.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Au nom de la rose' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.rosolin.AnnounceEventStarted", null);
	}

	/**
	 * Spawns the Event Manager
	 */
	private void spawnEventManagers()
	{
		//Rose EVENT meneger
		final int EVENT_MANAGERS1[][] = 
		{
				//TODO добавть кординаты во всех городах
			{ 83272, 148392, -3430, 0 },
		};

		SpawnNPCs(EVENT_MANAGER, EVENT_MANAGERS1, _spawns);
	}

	/**
	 * Removes spawn Event Manager
	 */
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	private static void loadMultiSell()
	{
		if(MultiSellLoaded)
			return;
		for(File f : multiSellFiles)
			MultiSellHolder.getInstance().parseFile(f);
		MultiSellLoaded = true;
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
		if(MultiSellLoaded)
		{
			for(File s : multiSellFiles)
				MultiSellHolder.getInstance().remove(s);
			MultiSellLoaded = false;
		}
	}

	@Override
	public void onShutdown()
	{

	}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer))
		{
			long count = Util.rollDrop(1, 1, Config.EVENT_GLITTMEDAL_NORMAL_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp * 10000L, true);
			if(count > 0)
				addItem(killer.getPlayer(), EVENT_RED_ROSE, count);
				addItem(killer.getPlayer(), EVENT_BLUEB_ROSE, count);
				addItem(killer.getPlayer(), EVENT_WHITE_ROSE, count);
		}
	}
	
	public void Roseds()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, EVENT_RED_ROSE) >= 1)
		{
			show("scripts/events/rosolin/event_col_agent1_q0996_01.htm", player);
			return;
		}
		//show("scripts/events/rosolin/event_col_agent2_q0996_02.htm", player);//TODO что то типо иди нахуй у тебя не чего нет
		//return;
	}

}