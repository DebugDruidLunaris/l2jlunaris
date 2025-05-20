package jts.gameserver.handler.admincommands.impl;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.dao.OlympiadNobleDAO;
import jts.gameserver.data.StringHolder;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.BuyListHolder;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.data.xml.holder.ProductHolder;
import jts.gameserver.data.xml.parser.NpcParser;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.instancemanager.SpawnManager;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.olympiad.OlympiadDatabase;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.scripts.Scripts;
import jts.gameserver.tables.FishTable;
import jts.gameserver.tables.PetDataTable;
import jts.gameserver.tables.SkillTable;

public class AdminReload implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_reload,
		admin_reload_scripts,
		admin_reload_config,
		admin_reload_multisell,
		admin_reload_gmaccess,
		admin_reload_htm,
		admin_reload_qs,
		admin_reload_qs_help,
		admin_reload_skills,
		admin_reload_npc,
		admin_reload_spawn,
		admin_reload_fish,
		admin_reload_abuse,
		admin_reload_translit,
		admin_reload_shops,
		admin_reload_static,
		admin_reload_pets,
		admin_reload_locale,
		admin_reload_nobles,
		admin_reload_im
	}

	@SuppressWarnings({ "rawtypes", "incomplete-switch" })
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanReload)
			return false;

		switch(command)
		{
			case admin_reload:
				break;
			case admin_reload_config:
			{
				try
				{
					Config.load();
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Error: " + e.getMessage() + "!");
					return false;
				}
				activeChar.sendMessage("Config reloaded!");
				break;
			}

			case admin_reload_scripts:
			{
				if (!Scripts.getInstance().reload())
					activeChar.sendMessage(String.format("Scripts reloaded with errors. Loaded %d classes.",
							Scripts.getInstance().getClasses().size()));
				else
					activeChar.sendMessage(String.format("Scripts successfully reloaded. Loaded %d classes.",
							Scripts.getInstance().getClasses().size()));
				break;
			}
			case admin_reload_multisell:
			{
				try
				{
					MultiSellHolder.getInstance().reload();
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage("Multisell list reloaded!");
				break;
			}
			case admin_reload_gmaccess:
			{
				try
				{
					Config.loadGMAccess();
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
							player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
						else
							player.setPlayerAccess(Config.gmlist.get(new Integer(0)));
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage("GMAccess reloaded!");
				break;
			}
			case admin_reload_htm:
			{
				HtmCache.getInstance().reload();
				activeChar.sendMessage("HTML cache reloaded.");
				break;
			}
			case admin_reload_qs:
			{
				if(fullString.endsWith("all"))
					for(Player p : GameObjectsStorage.getAllPlayersForIterate())
						reloadQuestStates(p);
				else
				{
					GameObject t = activeChar.getTarget();

					if(t != null && t.isPlayer())
					{
						Player p = (Player) t;
						reloadQuestStates(p);
					}
					else
						reloadQuestStates(activeChar);
				}
				break;
			}
			case admin_reload_skills:
			{
				SkillTable.getInstance().reload();
				break;
			}
			case admin_reload_npc:
			{
				NpcParser.getInstance().reload();
				break;
			}
			case admin_reload_spawn:
			{
				ThreadPoolManager.getInstance().execute(new RunnableImpl(){
					@Override
					public void runImpl() throws Exception
					{
						SpawnManager.getInstance().reloadAll();
					}
				});
				break;
			}
			case admin_reload_fish:
			{
				FishTable.getInstance().reload();
				break;
			}
			case admin_reload_abuse:
			{
				Config.abuseLoad();
				break;
			}
//			case admin_reload_translit:
//			{
//				Strings.reload();
//				break;
//			}
			case admin_reload_shops:
			{
				BuyListHolder.reload();
				break;
			}
			case admin_reload_static:
			{
				//StaticObjectsTable.getInstance().reloadStaticObjects();
				break;
			}
			case admin_reload_pets:
			{
				PetDataTable.getInstance().reload();
				break;
			}
			case admin_reload_locale:
			{
				StringHolder.getInstance().reload();
				break;
			}
			case admin_reload_nobles:
			{
				OlympiadNobleDAO.getInstance().select();
				OlympiadDatabase.loadNoblesRank();
				break;
			}
			case admin_reload_im:
			{
				ProductHolder.getInstance().reload();
				break;
			}
		}
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/reload.htm"));
		return true;
	}

	private void reloadQuestStates(Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
			p.removeQuestState(qs.getQuest().getName());
		Quest.restoreQuestStates(p);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}