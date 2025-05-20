package events.L2Coins;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Util;
import jts.commons.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class L2Coins extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(L2Coins.class);
	private static double MOUSE_COIN_CHANCE = Config.EVENT_MOUSE_COIN_CHANCE;
	private static int MOUSE_COIN = Config.EVENT_MOUSE_COIN_ID;
	private static int MOUSE_COIN_MIN_COUNT = Config.EVENT_MOUSE_COIN_MIN_COUNT;
	private static int MOUSE_COIN_MAX_COUNT = Config.EVENT_MOUSE_COIN_MAX_COUNT;
	//private static int BASE_COIN_AFTER_RB = Config.EVENT_BASE_COIN_AFTER_RB;
	private static final int EVENT_MANAGER_ID = 36608;
	private static final int EVENT_THREE_ID = 36609;
	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();

	private static boolean _active = false;

	private void spawnEventManagers()
	{
		final int EVENT_MANAGERS[][] = 
		{ 
			{ 82247, 148605, -3472, 0 }, //Giran 1
			{ 81864, 148916, -3482, 14902 }, //Giran 2
			{ 81921, 148298, -3482, 47930 }, //Giran 3
		};

		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}

	private void spawnThree()
	{
		final int EVENT_THREE[][] = 
		{ 
			{ 82168, 148856, -3464, 0 }, //Giran 1
			{ 81672, 148856, -3464, 0 }, //Giran 2
			{ 81672, 148360, -3464, 0 }, //Giran 3
			{ 82168, 148360, -3464, 0 }, //Giran 4
		};

		SpawnNPCs(EVENT_THREE_ID, EVENT_THREE, _spawns);
	}

	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}

	private static boolean isActive()
	{
		return IsActive("L2Coins");
	}

	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("L2Coins", true))
		{
			spawnEventManagers();
			spawnThree();
			_log.info("Event 'L2Coins' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.coins.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'L2Coins' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("L2Coins", false))
		{
			unSpawnEventManagers();
			_log.info("Event 'L2Coins' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.coins.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'L2Coins' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public static void OnPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.coins.AnnounceEventStarted", null);
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			spawnThree();
			_log.info("Loaded Event: L2Coins [state: activated]");
		}
		else
			_log.info("Loaded Event: L2Coins [state: deactivated]");
	}

	@Override
	public void onReload() {}

	@Override
	public void onShutdown() {}

	/**
	 * Обработчик смерти мобов, управляющий эвентовым дропом
	 */
	public void onDeath(Creature cha, Creature killer)
	{
		if(Config.EVENT_MOUSE_ALTERNATIVE == true)
		{
			// Проверяем давать или нет бонус по разнице лвл персонажа с мобом
			if(_active && SimpleCheckDrop(cha, killer) && cha.isMonster() && !cha.isRaid() && killer.getLevel() >= 40 && Math.abs(cha.getLevel() - killer.getLevel()) <= Config.EVENT_MOUSE_ALTERNATIVE_LVL_GAP)
			{
				// Расчитываем шанс и количество дропа
				double chance = 0;
				int mouse_count = 0;
				if(cha.getLevel() >= 40 && cha.getLevel() < 61)
				{
					chance = Config.EVENT_MOUSE_ALT_CHANCE_LVL_40_60;
					mouse_count = Config.EVENT_MOUSE_ALT_COUNT_LVL_40_60;
				}
				else if(cha.getLevel() >= 61 && cha.getLevel() < 76)
				{
					chance = Config.EVENT_MOUSE_ALT_CHANCE_LVL_61_75;
					mouse_count = Config.EVENT_MOUSE_ALT_COUNT_LVL_61_75;
				}
				else if(cha.getLevel() >= 76 && cha.getLevel() < 81)
				{
					chance = Config.EVENT_MOUSE_ALT_CHANCE_LVL_76_80;
					mouse_count = Config.EVENT_MOUSE_ALT_COUNT_LVL_76_80;
				}
				else if(cha.getLevel() > 80)
				{
					chance = Config.EVENT_MOUSE_ALT_CHANCE_LVL_81_85;
					mouse_count = Config.EVENT_MOUSE_ALT_COUNT_LVL_81_85;
				}
				if(Config.EVENT_MOUSE_ALTERNATIVE_RATE == true)
				{
					chance = chance * 1;
					mouse_count = mouse_count * (int) Config.RATE_DROP_ITEMS;
				}
				if(Rnd.chance(chance))
				{
					((NpcInstance) cha).dropItem(killer.getPlayer(), MOUSE_COIN, Rnd.get(MOUSE_COIN_MIN_COUNT, mouse_count));
				}
			}
		}
		else
		{
			if(_active && SimpleCheckDrop(cha, killer) && cha.getLevel() >= 80 && cha.isMonster() && !cha.isRaid() && Math.abs(cha.getLevel() - killer.getLevel()) < 3)
				((NpcInstance) cha).dropItem(killer.getPlayer(), MOUSE_COIN, Util.rollDrop(MOUSE_COIN_MIN_COUNT, MOUSE_COIN_MAX_COUNT, MOUSE_COIN_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp * 10000L, true));
		}

		/**
		 * TODO Доделать расчёты.
		 * 	// При убийстве РБ выше, чем 70 лвл, даётся много вкусностей
			if(_active && SimpleCheckDrop(character, killer) && character.getLevel() >= 80 && character.isRaid() && Math.abs(character.getLevel() - killer.getLevel()) < 3)
				// Даём много итемов, всем пати мемберам
				try
				{
					if(killer instanceof Playable)
					{
						final Player player = killer.getPlayer();
						if(player == null)
							return;

						if(player.getParty() != null)
							// Даём каждому
							for(@SuppressWarnings("unused")
							Player pl : player.getParty().getPartyMembers())
							{
								// Формула расчёт своя, если что редактируем
								int count = BASE_COIN_AFTER_RB * (character.getLevel() - 69) / player.getParty().getPartyMembers().size();
								if(count > 0)
									((NpcInstance) character).dropItem(killer.getPlayer(), MOUSE_COIN, count);
							}
						else
						{
							// Убил один РБ (???), то получаешь все итемы только ты
							// Если подозрение, что с пати будут выходить.
							int count = BASE_COIN_AFTER_RB * (character.getLevel() - 69);
							if(count > 0)
								addItem(killer.getPlayer(), MOUSE_COIN, count);
						}
					}
				}
				catch(final Exception e)
				{
					e.printStackTrace();
				}*/

	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.coins.AnnounceEventStarted", null);
	}
}