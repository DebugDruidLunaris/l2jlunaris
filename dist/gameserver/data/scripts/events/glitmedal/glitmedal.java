package events.glitmedal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jts.commons.util.Rnd;
import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.listener.actor.OnDeathListener;
import jts.gameserver.listener.actor.player.OnPlayerEnterListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.actor.listener.CharListenerList;
import jts.gameserver.model.instances.MonsterInstance;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class glitmedal extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static int EVENT_MANAGER_ID1 = 31228; // Roy
	private static int EVENT_MANAGER_ID2 = 31229; // Winnie

	private static final Logger _log = LoggerFactory.getLogger(glitmedal.class);

	// For temporary status that is given in the game randomly either 0 or 1
	private int isTalker;

	// Medals
	private static int EVENT_MEDAL = 6392;
	private static int EVENT_GLITTMEDAL = 6393;

	private static int Badge_of_Rabbit = 6399;
	private static int Badge_of_Hyena = 6400;
	private static int Badge_of_Fox = 6401;
	private static int Badge_of_Wolf = 6402;

	private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
	private static boolean _active = false;
	private static boolean MultiSellLoaded = false;

	private static File[] multiSellFiles =
			{
			new File("data/xml/other/event/glitmedal/502.xml"),
			new File("data/xml/other/event/glitmedal/503.xml"),
			new File("data/xml/other/event/glitmedal/504.xml"),
			new File("data/xml/other/event/glitmedal/505.xml"),
			new File("data/xml/other/event/glitmedal/506.xml"),
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
			_log.info("Loaded Event: L2 Medal Collection Event [state: activated]");
		}
		else
			_log.info("Loaded Event: L2 Medal Collection Event [state: deactivated]");
	}

	/**
	 * Reads the status of the opening event of the base.
	 *
	 * @return
	 */
	private static boolean isActive()
	{
		return IsActive("glitter");
	}

	/**
	 * Starts EVENT
	 */
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(SetActive("glitter", true))
		{
			loadMultiSell();
			spawnEventManagers();
			System.out.println("Event 'L2 Medal Collection Event' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'L2 Medal Collection Event' already started.");

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
		if(SetActive("glitter", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'L2 Medal Collection Event' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.glitmedal.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'L2 Medal Collection Event' not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.glitmedal.AnnounceEventStarted", null);
	}

	/**
	 * Spawns the Event Manager
	 */
	private void spawnEventManagers()
	{
		//1st cat EVENT
		final int EVENT_MANAGERS1[][] = 
		{
			{ 147893, -56622, -2776, 0 },
			{ -81070, 149960, -3040, 0 },
			{ 82882, 149332, -3464, 49000 },
			{ 44176, -48732, -800, 33000 },
			{ 147920, 25664, -2000, 16384 },
			{ 117498, 76630, -2695, 38000 },
			{ 111776, 221104, -3543, 16384 },
			{ -84516, 242971, -3730, 34000 },
			{ -13073, 122801, -3117, 0 },
			{ -44337, -113669, -224, 0 },
			{ 11281, 15652, -4584, 25000 },
			{ 44122, 50784, -3059, 57344 },
			{ 80986, 54504, -1525, 32768 },
			{ 114733, -178691, -821, 0 },
			{ 18178, 145149, -3054, 7400 }, 
		};

		// EVENT 2nd cat
		final int EVENT_MANAGERS2[][] = 
		{
			{ 147960, -56584, -2776, 0 },
			{ -81070, 149860, -3040, 0 },
			{ 82798, 149332, -3464, 49000 },
			{ 44176, -48688, -800, 33000 },
			{ 147985, 25664, -2000, 16384 },
			{ 117459, 76664, -2695, 38000 },
			{ 111724, 221111, -3543, 16384 },
			{ -84516, 243015, -3730, 34000 },
			{ -13073, 122841, -3117, 0 },
			{ -44342, -113726, -240, 0 },
			{ 11327, 15682, -4584, 25000 },
			{ 44157, 50827, -3059, 57344 },
			{ 80986, 54452, -1525, 32768 },
			{ 114719, -178742, -821, 0 },
			{ 18154, 145192, -3054, 7400 },
		};

		SpawnNPCs(EVENT_MANAGER_ID1, EVENT_MANAGERS1, _spawns);
		SpawnNPCs(EVENT_MANAGER_ID2, EVENT_MANAGERS2, _spawns);
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
			for(File f : multiSellFiles)
				MultiSellHolder.getInstance().remove(f);
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
				addItem(killer.getPlayer(), EVENT_MEDAL, count);
			if(killer.getPlayer().getInventory().getCountOf(Badge_of_Wolf) == 0 && Rnd.chance(Config.EVENT_GLITTMEDAL_GLIT_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp))
				addItem(killer.getPlayer(), EVENT_GLITTMEDAL, 1);
		}
	}

	public void glitchang()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, EVENT_MEDAL) >= 1000)
		{
			removeItem(player, EVENT_MEDAL, 1000);
			addItem(player, EVENT_GLITTMEDAL, 10);
			return;
		}
		player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
	}

	public void medal()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Wolf) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent1_q0996_05.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent1_q0996_04.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent1_q0996_03.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent1_q0996_02.htm", player);
			return;
		}

		show("scripts/events/glitmedal/event_col_agent1_q0996_01.htm", player);
	}

	public void medalb()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Wolf) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent2_q0996_05.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent2_q0996_04.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent2_q0996_03.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			show("scripts/events/glitmedal/event_col_agent2_q0996_02.htm", player);
			return;
		}

		show("scripts/events/glitmedal/event_col_agent2_q0996_01.htm", player);
		return;
	}

	public void game()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
			{
				show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
				return;
			}
			show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
			{
				show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
				return;
			}
			show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
			return;
		}
		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
			{
				show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
				return;
			}
			show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
			return;
		}

		else if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
		{
			show("scripts/events/glitmedal/event_col_agent2_q0996_11.htm", player);
			return;
		}

		show("scripts/events/glitmedal/event_col_agent2_q0996_12.htm", player);
	}

	public void gamea()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;
		isTalker = Rnd.get(2);

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Fox, 1);
					removeItem(player, EVENT_GLITTMEDAL, getItemCount(player, EVENT_GLITTMEDAL));
					addItem(player, Badge_of_Wolf, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_24.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 40);
					show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Hyena, 1);
					removeItem(player, EVENT_GLITTMEDAL, 20);
					addItem(player, Badge_of_Fox, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_23.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 20);
					show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Rabbit, 1);
					removeItem(player, EVENT_GLITTMEDAL, 10);
					addItem(player, Badge_of_Hyena, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_22.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 10);
					show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
			if(isTalker == 1)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				addItem(player, Badge_of_Rabbit, 1);
				show("scripts/events/glitmedal/event_col_agent2_q0996_21.htm", player);
				return;
			}
			else if(isTalker == 0)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				show("scripts/events/glitmedal/event_col_agent2_q0996_25.htm", player);
				return;
			}
		show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
	}

	// FIXME: нафига две идентичные функции?
	public void gameb()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
			return;
		isTalker = Rnd.get(2);

		if(getItemCount(player, Badge_of_Fox) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 40)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Fox, 1);
					removeItem(player, EVENT_GLITTMEDAL, 40);
					addItem(player, Badge_of_Wolf, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_34.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 40);
					show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		else if(getItemCount(player, Badge_of_Hyena) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 20)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Hyena, 1);
					removeItem(player, EVENT_GLITTMEDAL, 20);
					addItem(player, Badge_of_Fox, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_33.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 20);
					show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		else if(getItemCount(player, Badge_of_Rabbit) >= 1)
		{
			if(getItemCount(player, EVENT_GLITTMEDAL) >= 10)
				if(isTalker == 1)
				{
					removeItem(player, Badge_of_Rabbit, 1);
					removeItem(player, EVENT_GLITTMEDAL, 10);
					addItem(player, Badge_of_Hyena, 1);
					show("scripts/events/glitmedal/event_col_agent2_q0996_32.htm", player);
					return;
				}
				else if(isTalker == 0)
				{
					removeItem(player, EVENT_GLITTMEDAL, 10);
					show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
					return;
				}
			show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
			return;
		}

		if(getItemCount(player, EVENT_GLITTMEDAL) >= 5)
			if(isTalker == 1)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				addItem(player, Badge_of_Rabbit, 1);
				show("scripts/events/glitmedal/event_col_agent2_q0996_31.htm", player);
				return;
			}
			else if(isTalker == 0)
			{
				removeItem(player, EVENT_GLITTMEDAL, 5);
				show("scripts/events/glitmedal/event_col_agent2_q0996_35.htm", player);
				return;
			}
		show("scripts/events/glitmedal/event_col_agent2_q0996_26.htm", player);
		return;
	}
}