package jts.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.handler.admincommands.IAdminCommandHandler;
import jts.gameserver.instancemanager.SoDManager;
import jts.gameserver.instancemanager.SoIManager;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObject;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.instances.NpcInstance;
import jts.gameserver.network.serverpackets.EventTrigger;
import jts.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import jts.gameserver.network.serverpackets.ExSendUIEvent;
import jts.gameserver.network.serverpackets.L2GameServerPacket;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.NpcHtmlMessage;
import jts.gameserver.network.serverpackets.NpcSay;
import jts.gameserver.network.serverpackets.PlaySound;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.network.serverpackets.components.NpcString;
import jts.gameserver.scripts.Functions;
import jts.gameserver.stats.Stats;

import org.apache.commons.lang3.math.NumberUtils;

public class AdminAdmin implements IAdminCommandHandler
{
	private static List<String> _l = new ArrayList<String>();

	private static enum Commands
	{
		admin_admin,
		admin_play_sounds,
		admin_play_sound,
		admin_silence,
		admin_tradeoff,
		admin_cfg,
		admin_config,
		admin_show_html,
		admin_setnpcstate,
		admin_setareanpcstate,
		admin_showmovie,
		admin_setzoneinfo,
		admin_eventtrigger,
		admin_debug,
		admin_uievent,
		admin_opensod,
		admin_closesod,
		admin_setsoistage,
		admin_soinotify,
		admin_forcenpcinfo,
		admin_loc,
		admin_locdump,
		admin_undying,
		admin_giveall,
		admin_say
	}

	@SuppressWarnings({ "rawtypes", "incomplete-switch" })
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		GameObject target = activeChar.getTarget();
		Commands command = (Commands) comm;
		StringTokenizer st;

		if(activeChar.getPlayerAccess().Menu)
		{
			switch(command)
			{
				case admin_admin:
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
					break;
				case admin_play_sounds:
					if(wordList.length == 1)
						activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs.htm"));
					else
						try
						{
							activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
						}
						catch(StringIndexOutOfBoundsException e)
						{}
					break;
				case admin_play_sound:
					try
					{
						playAdminSound(activeChar, wordList[1]);
					}
					catch(StringIndexOutOfBoundsException e)
					{}
					break;
				case admin_silence:
					if(activeChar.getMessageRefusal()) // already in message refusal
					// mode
					{
						activeChar.unsetVar("gm_silence");
						activeChar.setMessageRefusal(false);
						activeChar.sendPacket(Msg.MESSAGE_ACCEPTANCE_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					else
					{
						if(Config.SAVE_GM_EFFECTS)
							activeChar.setVar("gm_silence", "true", -1);
						activeChar.setMessageRefusal(true);
						activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
						activeChar.sendEtcStatusUpdate();
					}
					break;
				case admin_tradeoff:
					try
					{
						if(wordList[1].equalsIgnoreCase("on"))
						{
							activeChar.setTradeRefusal(true);
							Functions.sendDebugMessage(activeChar, "tradeoff enabled");
						}
						else if(wordList[1].equalsIgnoreCase("off"))
						{
							activeChar.setTradeRefusal(false);
							Functions.sendDebugMessage(activeChar, "tradeoff disabled");
						}
					}
					catch(Exception ex)
					{
						if(activeChar.getTradeRefusal())
							Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
						else
							Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
					}
					break;
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
						else
							Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					catch(Exception npe)
					{
						Functions.sendDebugMessage(activeChar, "Html page not found");
					}
					break;
				case admin_setnpcstate:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
						return false;
					}
					int state;
					try
					{
						state = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify state");
						return false;
					}
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "You must target an NPC");
						return false;
					}
					NpcInstance npc = (NpcInstance) target;
					npc.setNpcState(state);
					break;
				case admin_setareanpcstate:
					try
					{
						final String val = fullString.substring(15).trim();

						String[] vals = val.split(" ");
						int range = NumberUtils.toInt(vals[0], 0);
						int astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

						for(NpcInstance n : activeChar.getAroundNpc(range, 200))
							n.setNpcState(astate);
					}
					catch(Exception e)
					{
						Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
					}
					break;
				case admin_showmovie:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
						return false;
					}
					int id;
					try
					{
						id = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.showQuestMovie(id);
					break;
				case admin_setzoneinfo:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo id");
						return false;
					}
					int stateid;
					try
					{
						stateid = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new ExChangeClientEffectInfo(stateid));
					break;
				case admin_eventtrigger:
					if(wordList.length < 2)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
						return false;
					}
					int triggerid;
					try
					{
						triggerid = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "You must specify id");
						return false;
					}
					activeChar.broadcastPacket(new EventTrigger(triggerid, true));
					break;
				case admin_debug:
					if(!target.isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Only player target is allowed");
						return false;
					}
					Player pl = target.getPlayer();
					List<String> _s = new ArrayList<String>();
					_s.add("==========TARGET STATS:");
					_s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
					_s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1, null, null));
					_s.add("==Skill Power: " + pl.calcStat(Stats.SKILL_POWER, 1, null, null));
					_s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1, null, null));

					_s.add("==========Powers:");
					_s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1, null, null));
					_s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1, null, null));
					_s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1, null, null));
					_s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1, null, null));
					_s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1, null, null));
					_s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1, null, null));
					_s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1, null, null));
					_s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1, null, null));
					_s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1, null, null));

					_s.add("==========PvP Stats:");
					_s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1, null, null));
					_s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1, null, null));
					_s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1, null, null));
					_s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1, null, null));
					_s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1, null, null));
					_s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1, null, null));

					_s.add("==========Reflects:");
					_s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
					_s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
					_s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
					_s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));

					_s.add("==========MP Consume Rate:");
					_s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1, null, null));
					_s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1, null, null));
					_s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1, null, null));

					_s.add("==========Shield:");
					_s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
					_s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
					_s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));

					_s.add("==========Etc:");
					_s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
					_s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.PSKILL_EVASION, null, null));
					_s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
					_s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
					_s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1, null, null));
					_s.add("==========DONE.");

					for(String s : _s)
						Functions.sendDebugMessage(activeChar, s);
					break;
				case admin_uievent:
					if(wordList.length < 5)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
						return false;
					}
					boolean hide;
					boolean increase;
					int startTime;
					int endTime;
					String text;
					try
					{
						hide = Boolean.parseBoolean(wordList[1]);
						increase = Boolean.parseBoolean(wordList[2]);
						startTime = Integer.parseInt(wordList[3]);
						endTime = Integer.parseInt(wordList[4]);
						text = wordList[5];
					}
					catch(NumberFormatException e)
					{
						Functions.sendDebugMessage(activeChar, "Invalid format");
						return false;
					}
					activeChar.broadcastPacket(new ExSendUIEvent(activeChar, hide, increase, startTime, endTime, text));
					break;
				case admin_opensod:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //opensod minutes");
						return false;
					}
					SoDManager.openSeed(Integer.parseInt(wordList[1]) * 60 * 1000L);
					break;
				case admin_closesod:
					SoDManager.closeSeed();
					break;
				case admin_setsoistage:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //setsoistage stage[1-5]");
						return false;
					}
					SoIManager.setCurrentStage(Integer.parseInt(wordList[1]));
					break;
				case admin_soinotify:
					if(wordList.length < 1)
					{
						Functions.sendDebugMessage(activeChar, "USAGE: //soinotify [1-3]");
						return false;
					}
					switch(Integer.parseInt(wordList[1]))
					{
						case 1:
							SoIManager.notifyCohemenesKill();
							break;
						case 2:
							SoIManager.notifyEkimusKill();
							break;
						case 3:
							SoIManager.notifyHoEDefSuccess();
							break;
					}
					break;
				case admin_forcenpcinfo:
					if(!target.isNpc())
					{
						Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
						return false;
					}
					((NpcInstance) target).broadcastCharInfo();
					break;
				case admin_loc:
					Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
					break;
				case admin_locdump:
					st = new StringTokenizer(fullString, " ");
					try
					{
						st.nextToken();
						try
						{
							new File("dumps").mkdir();
							File f = new File("dumps/locdump.txt");
							if(!f.exists())
								f.createNewFile();
							Functions.sendDebugMessage(activeChar, "Coords Dump Saved: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z );
							FileWriter writer = new FileWriter(f, true);
							writer.write("<coords loc=\"" + activeChar.getLoc().x + " " + activeChar.getLoc().y + " " + activeChar.getLoc().z +"\"/>"+"\n"); // " "+ activeChar.getLoc().h + 
							writer.close();
						}
						catch(Exception e) {}
					}
					catch(Exception e) {} // Case of wrong monster data
					break;
				case admin_undying:
					if(activeChar.isUndying())
					{
						activeChar.setUndying(false);
						Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
					}
					else
					{
						activeChar.setUndying(true);
						Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
					}
					break;
				case admin_giveall:
					if(wordList.length >= 3)
					{
						int _id = 0;
						int _count = 0;
						try
						{
							_id = Integer.parseInt(wordList[1]);
							_count = Integer.parseInt(wordList[2]);
						}
						catch(NumberFormatException e)
						{
							activeChar.sendMessage("only numbers");
							return false;
						}

						for(Player player : GameObjectsStorage.getAllPlayersForIterate())
						{
							if(player == null)
								continue;
							if(!checkPlayersIP(player))
								continue;
							Functions.addItem(player, _id, _count);
							player.sendMessage("You have been rewarded!");
							player.broadcastPacket(new MagicSkillUse(player, player, 6234, 1, 1000, 0));
						}
						_l.clear();
					}
					else
					{
						activeChar.sendMessage("USAGE: //giveall itemId count");
						return false;
					}
					return true;
				case admin_say:
					if(wordList.length < 3)
					{
						activeChar.sendMessage("USAGE: //say chattype message");
						return false;
					}
					ChatType type = ChatType.valueOf(wordList[1]);
					NpcString npcString = NpcString.NONE;
					String[] arg = new String[5];
					try
					{
						npcString = NpcString.valueOf(wordList[2]);
					}
					catch(IllegalArgumentException e)
					{
						arg[0] = wordList[2];
					}

					L2GameServerPacket packet = null;
					if(target.isNpc())
						packet = new NpcSay((NpcInstance) target, type, npcString, arg);
					else
						packet = new Say2(target.getObjectId(), type, target.getName(), npcString, arg);

					((Creature) target).broadcastPacket(packet);
					return true;
			}
			return true;
		}

		if(activeChar.getPlayerAccess().CanTeleport)
		{
			switch(command)
			{
				case admin_show_html:
					String html = wordList[1];
					try
					{
						if(html != null)
							if(html.startsWith("tele"))
								activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
							else
								activeChar.sendMessage("Access denied");
						else
							activeChar.sendMessage("Html page not found");
					}
					catch(Exception npe)
					{
						activeChar.sendMessage("Html page not found");
					}
					break;
			}
			return true;
		}

		return false;
	}

	private static boolean checkPlayersIP(Player player)
	{
		if(player == null)
			return false;

		if(_l.contains(player.getIP()))
			return false;

		_l.add(player.getIP());

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	public void playAdminSound(Player activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySound(sound));
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
		activeChar.sendMessage("Playing " + sound + ".");
	}
}