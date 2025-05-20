package events.Simple;

import java.io.File;

import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.instancemanager.SpawnManager;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simple extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(Simple.class);

	public static double ITEM_CHANCE = Config.SIMPLE_COIN_CHANCE;
	public static final int[] ITEM = Config.SIMPLE_COIN;
	public static int ITEM_MIN_COUNT = Config.SIMPLE_COIN_MIN_COUNT;
	public static int ITEM_MAX_COUNT = Config.SIMPLE_COIN_MAX_COUNT;
	public static int MONSTER_MIN_LEVEL = Config.SIMPLE_MONSTER_MIN_LEVEL;
	public static int MONSTER_MAX_LEVEL = Config.SIMPLE_MONSTER_MAX_LEVEL;

	public static double ITEM_CHANCE_RB = Config.SIMPLE_COIN_CHANCE_RB;
	private static final int[] ITEM_RB = Config.SIMPLE_COIN_RB;
	public static int ITEM_MIN_COUNT_RB = Config.SIMPLE_COIN_MIN_COUNT_RB;
	public static int ITEM_MAX_COUNT_RB = Config.SIMPLE_COIN_MAX_COUNT_RB;
	public static int MONSTER_MIN_LEVEL_RB = Config.SIMPLE_MONSTER_MIN_LEVEL_RB;
	public static int MONSTER_MAX_LEVEL_RB = Config.SIMPLE_MONSTER_MAX_LEVEL_RB;

	public static int EVENT_MANAGER_ID = Config.SIMPLE_EVENT_MANAGER_ID;
	public static int EVENT_MANAGER_MULTISELL = Config.SIMPLE_EVENT_MANAGER_MULTISELL;

	private static boolean _active = false;
	private static boolean MultiSellLoaded = false;

	private static File multiSellFile = new File("data/xml/other/event/Simple/" + EVENT_MANAGER_MULTISELL + ".xml");

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			loadMultiSell();
			spawnEventManagers();
			_log.info("Loaded Event: Simple [state: activated]");
		}
		else
			_log.info("Loaded Event: Simple [state: deactivated]");
	}

	/**
	 * Читает статус ивента из базы.
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("SimpleEvent");
	}

	/**
	 * Запускает ивент
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("SimpleEvent", true))
		{
			loadMultiSell();
			spawnEventManagers();
			_log.info("Event 'Simple' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.simple.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Simple' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	/**
	 * Останавливает ивент
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("SimpleEvent", false))
		{
			unSpawnEventManagers();
			_log.info("Event 'Simple' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.simple.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Simple' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	/**
	 * Спавнит ивент менеджеров
	 */
	private void spawnEventManagers()
	{
		SpawnManager.getInstance().spawn("simple_event");
		_log.info("SpawnManager: spawned npc for event: Simple");
	}

	/**
	 * Удаляет спавн ивент менеджеров
	 */
	private void unSpawnEventManagers()
	{
		SpawnManager.getInstance().despawn("simple_event");
		_log.info("SpawnManager: despawned npc for event: Simple");
	}

	/**
	 * Загружаем мультиселл.
	 */
	private static void loadMultiSell()
	{
		if(!MultiSellLoaded)
			return;
		MultiSellHolder.getInstance().parseFile(multiSellFile);
		MultiSellLoaded = true;
	}

	@Override
	public void onReload()
	{
		unSpawnEventManagers();
		if(MultiSellLoaded)
		{
			MultiSellHolder.getInstance().remove(multiSellFile);
			MultiSellLoaded = false;
		}
	}

	@Override
	public void onShutdown()
	{

	}

	/**
	 * Обработчик смерти мобов, управляющий ивентовым дропом
	 */
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(cha == null)
			return;

		if(cha.isPlayer() || cha.isPet() || cha.isSummon() || cha.isDoor() || (cha.isMonster() && (cha.getLevel() < MONSTER_MIN_LEVEL || cha.getLevel() > MONSTER_MAX_LEVEL)) || (cha.isRaid() && (cha.getLevel() < MONSTER_MIN_LEVEL_RB || cha.getLevel() > MONSTER_MAX_LEVEL_RB)))
			return;

		int CHARACTER_RATE_HP = 1;

		if(Config.SIMPLE_RATE_ITEM_BY_HP && _active)
		{
			try
			{
				CHARACTER_RATE_HP = ((int) ((NpcInstance) cha).getTemplate().rateHp);
			}
			catch(ClassCastException e)
			{
				// Тут исключение часто выбивает, позже нужно изучить, иначе засирает ГС
				_log.info("DEBUG SIMPLE: " + e);
				_log.info("DEBUG SIMPLE (NPC): " + cha.getName() + " (ID): " + cha.getNpcId());
			}
		}

		if(_active && SimpleCheckDrop(cha, killer) && Rnd.chance(ITEM_CHANCE * killer.getPlayer().getRateItems()))
			((NpcInstance) cha).dropItem(killer.getPlayer(), ITEM[Rnd.get(ITEM.length)], Rnd.get(ITEM_MIN_COUNT * CHARACTER_RATE_HP, ITEM_MAX_COUNT * CHARACTER_RATE_HP));

		//TODO: Проверить работу на всех видах рб. Возможно в метод "SimpleCheckDropRaid" добавить "raid.getEffectList().getEffectsBySkillId(4494) != null" для проверки наличия скила рб.
		if(_active && SimpleCheckDropRaid(cha, killer) && Rnd.chance(ITEM_CHANCE_RB * killer.getPlayer().getRateItems()))
			((NpcInstance) cha).dropItem(killer.getPlayer(), ITEM_RB[Rnd.get(ITEM_RB.length)], Rnd.get(ITEM_MIN_COUNT_RB * CHARACTER_RATE_HP, ITEM_MAX_COUNT_RB * CHARACTER_RATE_HP));
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.simple.AnnounceEventStarted", null);
	}
}