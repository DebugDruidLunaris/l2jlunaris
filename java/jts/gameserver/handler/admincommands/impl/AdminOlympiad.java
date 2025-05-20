package jts.gameserver.handler.admincommands.impl;

import java.util.ArrayList;
import java.util.List;

import jts.gameserver.Announcements;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.entity.Hero;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.model.entity.olympiad.OlympiadDatabase;
import jts.gameserver.model.entity.olympiad.OlympiadEndTask;
import jts.gameserver.model.entity.olympiad.OlympiadManager;
import jts.gameserver.model.entity.olympiad.ValidationTask;
import jts.gameserver.templates.StatsSet;
import jts.gameserver.utils.Log_New;

public class AdminOlympiad implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_oly_save,
		admin_add_oly_points,
		admin_oly_start,
		admin_add_hero,
		admin_oly_stop,
		admin_olympiad_stop_period,
		admin_olympiad_start_period
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_oly_save:
			{
				if(!Config.OLYMPIAD_ENABLE)
					return false;

				try
				{
					OlympiadDatabase.save();
				}
				catch(Exception e)
				{

				}
				activeChar.sendMessage("olympaid data saved.");
				break;
			}
			case admin_add_oly_points:
			{
				if(wordList.length < 3)
				{
					activeChar.sendMessage("Command syntax: //add_oly_points <char_name> <point_to_add>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}

				int pointToAdd;

				try
				{
					pointToAdd = Integer.parseInt(wordList[2]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Please specify integer value for olympiad points.");
					return false;
				}

				int curPoints = Olympiad.getNoblePoints(player.getObjectId());
				Olympiad.manualSetNoblePoints(player.getObjectId(), curPoints + pointToAdd);
				int newPoints = Olympiad.getNoblePoints(player.getObjectId());

				activeChar.sendMessage("Added " + pointToAdd + " points to character " + player.getName());
				activeChar.sendMessage("Old points: " + curPoints + ", new points: " + newPoints);
				Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", new String[] { "Olympiad Points : " + player.getName() + "to" +curPoints+" + add " + pointToAdd + "" });

				break;
			}
			case admin_oly_start:
			{
				Olympiad._manager = new OlympiadManager();
				Olympiad._inCompPeriod = true;

				new Thread(Olympiad._manager).start();

				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
				break;
			}
			case admin_oly_stop:
			{
				Olympiad._inCompPeriod = false;
				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
				try
				{
					OlympiadDatabase.save();
				}
				catch(Exception e)
				{

				}

				break;
			}
			case admin_add_hero:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Command syntax: //add_hero <char_name>");
					activeChar.sendMessage("This command can be applied only for online players.");
					return false;
				}

				Player player = World.getPlayer(wordList[1]);
				if(player == null)
				{
					activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
					return false;
				}

				StatsSet hero = new StatsSet();
				hero.set(Olympiad.CLASS_ID, player.getBaseClassId());
				hero.set(Olympiad.CHAR_ID, player.getObjectId());
				hero.set(Olympiad.CHAR_NAME, player.getName());

				List<StatsSet> heroesToBe = new ArrayList<StatsSet>();
				heroesToBe.add(hero);

				Hero.getInstance().computeNewHeroes(heroesToBe);

				activeChar.sendMessage("Hero status added to player " + player.getName());
				Log_New.LogEvent(activeChar.getName(), activeChar.getIP(), "AdminCommands", new String[] { "Add_hero : Hero status added to player " + player.getName() + " to" +heroesToBe+"  " });

				break;
			}
			case admin_olympiad_stop_period:
			{
				Olympiad.cancelPeriodTasks();
				ThreadPoolManager.getInstance().execute(new OlympiadEndTask());
				break;
			}
			case admin_olympiad_start_period:
			{
				Olympiad.cancelPeriodTasks();
				ThreadPoolManager.getInstance().execute(new ValidationTask());
				break;
			}
		}

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}