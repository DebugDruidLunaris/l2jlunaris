package jts.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jts.commons.collections.MultiValueSet;
import jts.gameserver.Config;
import jts.gameserver.ai.CharacterAI;
import jts.gameserver.data.xml.holder.NpcHolder;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.instancemanager.RaidBossSpawnManager;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.Player;
import jts.gameserver.model.SimpleSpawner;
import jts.gameserver.model.Spawner;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.scripts.Scripts;
import jts.gameserver.templates.npc.NpcTemplate;

public class AdminSpawn implements IAdminCommandHandler
{

	private static enum Commands
	{
		admin_show_spawns,
		admin_spawn,
		admin_spawn_monster,
		admin_spawn_index,
		admin_spawn1,
		admin_pspawn,
		admin_setheading,
		admin_setai,
		admin_setaiparam,
		admin_dumpparams
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;
		StringTokenizer st;
		NpcInstance target;
		Spawner spawn;
		NpcInstance npc;
		switch(command)
		{
			case admin_show_spawns:
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns.htm"));
				break;
			case admin_spawn_index:
				try
				{
					String val = fullString.substring(18);
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
				}
				catch(StringIndexOutOfBoundsException e)
				{}
				break;
			case admin_spawn1:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int mobCount = 1;
					if(st.hasMoreTokens())
					{
						mobCount = Integer.parseInt(st.nextToken());
					}
					spawnMonster(activeChar, id, 0, mobCount);
				}
				catch(Exception e) {} // Case of wrong monster data
				break;
			case admin_spawn:
			case admin_spawn_monster:
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int respawnTime = 30;
					int mobCount = 1;
					if(st.hasMoreTokens())
					{
						mobCount = Integer.parseInt(st.nextToken());
					}
					if(st.hasMoreTokens())
					{
						respawnTime = Integer.parseInt(st.nextToken());
					}

					spawnMonster(activeChar, id, respawnTime, mobCount);
				}
				catch(Exception e) {} // Case of wrong monster data
				break;
			case admin_setai:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Пожалуйста, выберите NPC или моба.");
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Пожалуйста, укажите AI");
					return false;
				}
				String aiName = st.nextToken();
				target = (NpcInstance) activeChar.getTarget();

				Constructor<?> aiConstructor = null;
				try
				{
					if(!aiName.equalsIgnoreCase("npc"))
					{
						aiConstructor = Class.forName("jts.gameserver.ai." + aiName).getConstructors()[0];
					}
				}
				catch(Exception e)
				{
					try
					{
						aiConstructor = Scripts.getInstance().getClasses().get("ai." + aiName).getConstructors()[0];
					}
					catch(Exception e1)
					{
						activeChar.sendMessage("Этот AI не найден.");
						return false;
					}
				}

				if(aiConstructor != null)
				{
					try
					{
						target.setAI((CharacterAI) aiConstructor.newInstance(new Object[] { target }));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					target.getAI().startAITask();
				}
				break;
			case admin_setaiparam:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Пожалуйста, выберите NPC или моба.");
					return false;
				}

				st = new StringTokenizer(fullString, " ");
				st.nextToken();

				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter name.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}

				String paramName = st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter value.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}
				String paramValue = st.nextToken();
				target = (NpcInstance) activeChar.getTarget();
				target.setParameter(paramName, paramValue);
				target.decayMe();
				target.spawnMe();
				activeChar.sendMessage("AI parameter " + paramName + " succesfully setted to " + paramValue);
				break;
			case admin_dumpparams:
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Пожалуйста, выберите NPC или моба.");
					return false;
				}
				target = (NpcInstance) activeChar.getTarget();
				MultiValueSet<String> set = target.getParameters();
				if(!set.isEmpty())
				{
					System.out.println("Dump of Parameters:\r\n" + set.toString());
				}
				else
				{
					System.out.println("Parameters is empty.");
				}
				break;
			case admin_setheading:
				GameObject obj = activeChar.getTarget();
				if(!obj.isNpc())
				{
					activeChar.sendMessage("Неверный Таргет");
					return false;
				}

				npc = (NpcInstance) obj;
				npc.setHeading(activeChar.getHeading());
				npc.decayMe();
				npc.spawnMe();
				activeChar.sendMessage("New heading : " + activeChar.getHeading());

				spawn = npc.getSpawn();
				if(spawn == null)
				{
					activeChar.sendMessage("Spawn for this npc == null!");
					return false;
				}
				break;
			case admin_pspawn:
				activeChar.sendMessage("Пример ВВода данных : id Количество  Респавн");
				activeChar.sendMessage("Пример  : 13860 1 60");
				st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String monsterId = st.nextToken();
					if(!(monsterId == null))
					{
						activeChar.sendMessage("Вы не ввели время респавна NPC!");
					}
					String respawnTime = st.nextToken();
					if(!(respawnTime == null))
					{
						activeChar.sendMessage("Вы не ввели Количество NPC!");
					}
					String mobCount = st.nextToken();
					 Date date = new Date();
					spawnMonster(activeChar, monsterId, 0, 1);
					if(Config.SAVE_ADMIN_SPAWN)
					{

						try
						{
							new File("data/xml/spawn/save/").mkdir();
							File f = new File(Config.DATAPACK_ROOT, "data/xml/spawn/save/Spawn_ID("+monsterId+")Date:"+ (new Timestamp(date.getTime())) +".xml");
							if(!f.exists())
							{
								f.createNewFile();
							}
							FileWriter writer = new FileWriter(f, true);
							writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
				            writer.write("<!DOCTYPE list SYSTEM \"spawn.dtd\">");
							writer.write("<list>\n");
							writer.write("\t<spawn count=\"" + mobCount + "\" respawn=\"" + respawnTime + "\" respawn_random=\"0\" period_of_day=\"none\">\n\t\t" + "<point x=\"" + activeChar.getLoc().x + "\" y=\"" + activeChar.getLoc().y + "\" z=\"" + activeChar.getLoc().z + "\" h=\"" + activeChar.getLoc().h + "\" />\n\t\t" + "<npc id=\"" + Integer.parseInt(monsterId) + "\" /><!--" + NpcHolder.getInstance().getTemplate(Integer.parseInt(monsterId)).getName() + "-->\n\t</spawn>\n");
							writer.write("</list>\n");
							writer.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				catch(Exception e) {} // Case of wrong monster data
				break;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
		{
			target = activeChar;
		}

		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher regexp = pattern.matcher(monsterId);
		NpcTemplate template;
		if(regexp.matches())
		{
			// First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template = NpcHolder.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template = NpcHolder.getInstance().getTemplateByName(monsterId);
		}

		if(template == null)
		{
			activeChar.sendMessage("Incorrect monster template.");
			return;
		}

		try
		{
			SimpleSpawner spawn = new SimpleSpawner(template);
			spawn.setLoc(target.getLoc());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			spawn.setReflection(activeChar.getReflection());

			if(RaidBossSpawnManager.getInstance().isDefined(template.getNpcId()))
			{
				activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
			}
			else
			{
				spawn.init();
				if(respawnTime == 0)
				{
					spawn.stopRespawn();
				}
				activeChar.sendMessage("Cоздание " + template.name + " По таргету " + target.getObjectId() + ".");
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}