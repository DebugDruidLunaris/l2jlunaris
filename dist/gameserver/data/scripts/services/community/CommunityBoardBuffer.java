package services.community;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import jts.gameserver.Config;
import jts.gameserver.common.Buff;
import jts.gameserver.common.BuffScheme;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.common.GenerateElement;
import jts.gameserver.common.Scheme;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Effect;
import jts.gameserver.model.Playable;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.skills.SkillType;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Util;

public class CommunityBoardBuffer implements ScriptFile, ICommunityBoardHandler
{
	private static final TIntArrayList allowBuff = new TIntArrayList(Config.BBS_BUFFER_ALLOWED_BUFF);
	private static ArrayList<String> pageBuffPlayer;
	private static ArrayList<String> pageBuffPet;
	private static StringBuilder buffSchemes = new StringBuilder();

	@Override
	public void onLoad()
	{
		if(Config.BBS_BUFFER_ALLOWED_BUFFER)
		{
			CommunityBoardManager.getInstance().registerHandler(this);

			pageBuffPlayer = new ArrayList<String>();
			pageBuffPet = new ArrayList<String>();

			genPageBuff(pageBuffPlayer, 0, "Player");
			genPage(pageBuffPlayer, "player");

			genPageBuff(pageBuffPet, 0, "Pet");
			genPage(pageBuffPet, "pet");

			BuffScheme.load();

			for(int id : BuffScheme.buffSchemes.keySet())
			{
				StringBuilder parametrs = new StringBuilder();
				parametrs.append("_bbscastgroupbuff ").append(id).append(" $Who");

				StringBuilder name = new StringBuilder();
				name.append(BuffScheme.buffSchemes.get(id).getName());

				buffSchemes.append(GenerateElement.button(name.toString(), parametrs.toString(), 169, 25, "L2UI_CT1.Button_DF_Down", "L2UI_CT1.Button_DF") + "<br1>" + Util.formatAdena(BuffScheme.buffSchemes.get(id).getPriceCount()) + " " + DifferentMethods.getItemName(BuffScheme.buffSchemes.get(id).getPriceId()));
			}
		}
		
	}
	

	@Override
	public void onReload()
	{
		CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown() {}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] 
		{
			"_bbsbuffer",
			"_bbsplayerbuffer",
			"_bbspetbuffer",
			"_bbscastbuff",
			"_bbscastgroupbuff",
			"_bbsbuffersave",
			"_bbsbufferuse",
			"_bbsbufferdelete",
			"_bbsbufferheal",
			"_bbsbufferremovebuffs" 
		};
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		if(!Config.BBS_BUFFER_ALLOWED_BUFFER)
		{
			DifferentMethods.communityNextPage(player, "_bbshome");
			return;
		}
        if (!Config.BBS_BUFFER_ALLOWED_PK && player.getKarma() > 0) 
		{
            player.sendMessage(player.isLangRus() ? "PK нельзя использовать Баффера" : "PK can not use a Buff");
            return;
        }
        if (!Config.BBS_BUFFER_ALLOWED_CURSED_WEAPON && player.isCursedWeaponEquipped()) 
		{
            player.sendMessage(player.isLangRus() ? "Баффера нельзя использовать с Заричем или Акаманахом" : "Buffer can not be used with Zaric or Akamanahom");
            return;
        }
		if (Config.CHECK_DEATH_TIME && System.currentTimeMillis()  < player._deathtime + Config.CHECK_DEATH_TIME_VAL ) 
		{
            player.sendMessage(player.isLangRus() ? "Игрок ослаблен после смерти " + Config.CHECK_DEATH_TIME_VAL /1000 +" секунд(ы). Осталось : " +  (player._deathtime + Config.CHECK_DEATH_TIME_VAL - System.currentTimeMillis()) / 1000+" секунд(ы). до Бафа" :"Player weakened after death " + Config.CHECK_DEATH_TIME_VAL /1000 +" sekond(s). Remaining : " +  (player._deathtime + Config.CHECK_DEATH_TIME_VAL - System.currentTimeMillis()) / 1000+" Seconds(s) .to Baffs");
            return;
        }
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/buffer/scheme.htm", player);
		if("bbsbuffer".equals(cmd))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/" + Config.BBS_FOLDER + "/buffer/index.htm", player);
			html = html.replace("%scheme%", buffSchemes.toString());
			StringBuilder schemes = new StringBuilder();
			schemes.append("<table>");
			for(String name : player.getSchemes().keySet())
			{
				schemes.append("<tr>");

				schemes.append("<td valign=\"top\" align=\"center\">");
				schemes.append(GenerateElement.button(name.substring(7), "_bbsbufferuse " + name + " $Who", 150, 25, "L2UI_CT1.Button_DF_Down", "L2UI_CT1.Button_DF"));
				schemes.append("</td>");

				schemes.append("<td valign=\"top\" align=\"center\"><table><tr><td></td></tr></table>");
				schemes.append(GenerateElement.button("", "_bbsbufferdelete " + name, 15, 15, "L2UI_CT1.Button_DF_Delete_Down", "L2UI_CT1.Button_DF_Delete"));
				schemes.append("</td>");

				schemes.append("</tr>");
			}
			schemes.append("</table>");
			html = html.replace("%buffgrps%", schemes.toString());
		}
		else if(bypass.startsWith("_bbsplayerbuffer"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, ":");
			st1.nextToken();
			int page = Integer.parseInt(st1.nextToken());
			if(pageBuffPlayer.get(page) != null)
			{
				html = html.replace("%content%", pageBuffPlayer.get(page));
			}
		}
		else if(bypass.startsWith("_bbspetbuffer"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, ":");
			st1.nextToken();
			int page = Integer.parseInt(st1.nextToken());
			if(pageBuffPet.get(page) != null)
			{
				html = html.replace("%content%", pageBuffPet.get(page));
			}
		}
		else if(bypass.startsWith("_bbscastbuff"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, ":");
			st1.nextToken();

			int id = Integer.parseInt(st1.nextToken());
			int level = Integer.parseInt(st1.nextToken());
			int page = Integer.parseInt(st1.nextToken());
			String type = st1.nextToken();

			Playable playable = null;
			if("Player".equals(type))
			{
				playable = player;
			}
			else if("Pet".equals(type))
			{
				playable = player.getPet();
			}

			int check = allowBuff.indexOf(id);

			if(playable != null && check != -1 && allowBuff.get(check + 1) <= level)
				if(player.getActiveClass().getLevel() < Config.BBS_BUFFER_MIN_LVL)
				{
					buff(id, level, playable);
				}
				else if(DifferentMethods.getPay(player, Config.BBS_BUFFER_PRICE_ID, Config.BBS_BUFFER_PRICE_ONE, true))
				{
					buff(id, level, playable);
				}

			if("Player".equals(type))
			{
				html = html.replace("%content%", pageBuffPlayer.get(page));
			}
			else if("Pet".equals(type))
			{
				html = html.replace("%content%", pageBuffPet.get(page));
			}
		}
		else if(bypass.startsWith("_bbscastgroupbuff"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, " ");
			st1.nextToken();
			int id = Integer.parseInt(st1.nextToken());
			String type = st1.nextToken();
			int priceId = BuffScheme.buffSchemes.get(id).getPriceId();
			int priceCount = BuffScheme.buffSchemes.get(id).getPriceCount();

			Playable playable = null;
			if("Player".equals(type))
			{
				playable = player;
			}
			else if("Pet".equals(type))
			{
				playable = player.getPet();
			}

			if(playable != null)
				if(player.getActiveClass().getLevel() < Config.CBB_BUFFER_FREE_LEVEL)
				{
					for(Buff buffId : BuffScheme.buffSchemes.get(id).getBuffIds())
					{
						buff(buffId.getId(), buffId.getLevel(), playable);
					}
				}
				else if(DifferentMethods.getPay(player, priceId, priceCount, true))
				{
					for(Buff buffId : BuffScheme.buffSchemes.get(id).getBuffIds())
					{
						buff(buffId.getId(), buffId.getLevel(), playable);
					}
				}

			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else if(bypass.startsWith("_bbsbuffersave"))
		{
			if(player.getSchemes().size() >= 5)
			{
				player.sendMessage("Можно сохранять не более 5ти схем.");
				DifferentMethods.communityNextPage(player, "_bbsbuffer");
				return;
			}

			StringTokenizer st1 = new StringTokenizer(bypass, " ");

			if(st1.countTokens() < 3)
			{
				DifferentMethods.communityNextPage(player, "_bbsbuffer");
				return;
			}

			st1.nextToken();

			String name = st1.nextToken();
			String type = st1.nextToken();

			Playable playable = null;
			if("Player".equals(type))
			{
				playable = player;
			}
			else if("Pet".equals(type))
			{
				playable = player.getPet();
			}

			if(playable == null)
				return;

			if(playable.getEffectList().getAllEffects().size() == 0)
			{
				DifferentMethods.communityNextPage(player, "_bbsbuffer");
				return;
			}

			if(DifferentMethods.getPay(player, Config.BBS_BUFFER_SAVE_PRICE_ID, Config.BBS_BUFFER_SAVE_PRICE_ONE, true))
			{
				StringBuilder buffs = new StringBuilder();
				Scheme scheme = new Scheme(name);
				for(Effect effect : playable.getEffectList().getAllEffects())
				{
					Skill skill = effect.getSkill();
					int id = skill.getId();
					int level = skill.getLevel();
					int check = allowBuff.indexOf(skill.getId());
					level = level > allowBuff.get(check + 1) ? allowBuff.get(check + 1) : level;

					if(check != -1)
					{
						buffs.append(id).append(",").append(level).append(";");
						scheme.addBuff(id, level);
					}
				}

				player.setVar("Scheme_" + name, buffs.toString(), -1);
				player.setScheme("Scheme_" + name, scheme);
			}

			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else if(bypass.startsWith("_bbsbufferuse"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, " ");
			st1.nextToken();
			String name = st1.nextToken();
			String type = st1.nextToken();

			Playable playable = null;
			if("Player".equals(type))
			{
				playable = player;
			}
			else if("Pet".equals(type))
			{
				playable = player.getPet();
			}
			if(playable != null)
			{
				for(Map.Entry<Integer, Integer> scheme : player.getScheme(name).getBuffs().entrySet())
					if(allowBuff.indexOf(scheme.getKey()) != -1)
						if(player.getActiveClass().getLevel() < Config.CBB_BUFFER_FREE_LEVEL)
						{
							buff(scheme.getKey(), scheme.getValue(), playable);
						}
						else if(DifferentMethods.getPay(player, Config.BBS_BUFFER_PRICE_ID, Config.BBS_BUFFER_PRICE_ONE, true))
						{
							buff(scheme.getKey(), scheme.getValue(), playable);
						}
			}
			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else if(bypass.startsWith("_bbsbufferdelete"))
		{
			StringTokenizer st1 = new StringTokenizer(bypass, " ");
			st1.nextToken();
			String name = st1.nextToken();
			player.unsetVar(name);
			player.removeScheme(name);
			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else if(bypass.startsWith("_bbsbufferheal"))
		{
			if(!Config.BBS_BUFFER_RECOVER_HP_MP_CP)
			{
				Functions.sendMessage("Сервис отключен.", player);
			}
			else
			{
				StringTokenizer st1 = new StringTokenizer(bypass, " ");
				st1.nextToken();
				String type = st1.nextToken();
				String target = st1.nextToken();

				Playable playable = null;
				if("Player".equals(target))
				{
					playable = player;
				}
				else if("Pet".equals(target))
				{
					playable = player.getPet();
				}

				if(playable != null)
				{
					if("HP".equals(type))
					{
						if(playable.getCurrentHp() != playable.getMaxHp())
						{
							playable.setCurrentHp(playable.getMaxHp(), true, true);
						}
						else
						{
							DifferentMethods.communityNextPage(player, "_bbsbuffer");
							return;
						}
					}
					else if("MP".equals(type))
					{
						if(playable.getCurrentMp() != playable.getMaxMp())
						{
							playable.setCurrentMp(playable.getMaxMp(), true);
						}
						else
						{
							DifferentMethods.communityNextPage(player, "_bbsbuffer");
							return;
						}
					}
					else if("CP".equals(type))
					{
						if(playable.getCurrentCp() != playable.getMaxCp())
						{
							playable.setCurrentCp(playable.getMaxCp(), true);
						}
						else
						{
							DifferentMethods.communityNextPage(player, "_bbsbuffer");
							return;
						}
					}
					else
					{
						DifferentMethods.clear(player);
					}

					playable.broadcastPacket(new MagicSkillUse(playable, playable, 6696, 1, 1000, 0));
				}
			}
			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else if(bypass.startsWith("_bbsbufferremovebuffs"))
		{
			if(!Config.BBS_BUFFER_CLEAR_BUFF)
			{
				Functions.sendMessage("Serviço está desligado.", player);
			}
			else
			{
				StringTokenizer st1 = new StringTokenizer(bypass, " ");
				st1.nextToken();
				String type = st1.nextToken();

				Playable playable = null;
				if("Player".equals(type))
				{
					playable = player;
				}
				else if("Pet".equals(type))
				{
					playable = player.getPet();
				}

				if(playable != null)
				{
					for(Effect effect : playable.getEffectList().getAllEffects())
						if(effect.getSkill().getSkillType() == SkillType.BUFF || effect.getSkill().getSkillType() == SkillType.COMBATPOINTHEAL)
						{
							effect.exit();
						}
					playable.broadcastPacket(new MagicSkillUse(playable, playable, 6696, 1, 1000, 0));
				}
			}
			DifferentMethods.communityNextPage(player, "_bbsbuffer");
			return;
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>" + new CustomMessage("communityboard.notdone", player).addString(bypass) + "</center><br><br></body></html>", player);
		}

		ShowBoard.separateAndSend(html, player);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{

	}

	private static void genPage(ArrayList<String> list, String type)
	{
		StringBuilder sb = new StringBuilder("<table><tr>");
		sb.append("<td width=70>Навигация: </td>");

		for(int i = 0; i < list.size(); i++)
		{
			sb.append(GenerateElement.buttonTD(String.valueOf(i + 1), "_bbs" + type + "buffer:" + i, 25, 25, "L2UI_CT1.Button_DF_Down", "L2UI_CT1.Button_DF"));
		}

		sb.append("<td>" + GenerateElement.button("Назад", "_bbsbuffer", 60, 25, "L2UI_CT1.Button_DF_Down", "L2UI_CT1.Button_DF") + "</td></tr></table><br><br>");

		for(int i = 0; i < list.size(); i++)
		{
			list.set(i, sb.toString() + list.get(i));
		}
	}

	private static void genPageBuff(ArrayList<String> list, int start, String type)
	{
		StringBuilder buffPages = new StringBuilder("<table><tr>");
		int i = start;
		Boolean next = false;
		for(; i < allowBuff.size(); i += 2)
		{
			if(next && i % 12 == 0)
			{
				buffPages.append("</tr><tr>");
			}
			if(next && i % (12 * 4) == 0)
			{
				break;
			}
			buffPages.append("<td>").append(buttonBuff(allowBuff.get(i), allowBuff.get(i + 1), list.size(), type)).append("</td>");
			next = true;
		}
		buffPages.append("</tr></table>");

		list.add(buffPages.toString());

		if(i + 2 <= allowBuff.size())
		{
			genPageBuff(list, i, type);
		}
	}

	private static String buttonBuff(int id, int level, int page, String type)
	{
		String skillId = Integer.toString(id);
		StringBuilder sb = new StringBuilder("<table width=100>");
		String icon;
		if(skillId.length() < 4)
		{
			icon = 0 + skillId;
		}
		else if(skillId.length() < 3)
		{
			icon = 00 + skillId;
		}
		else if(id == 4700 || id == 4699)
		{
			icon = "1331";
		}
		else if(id == 4702 || id == 4703)
		{
			icon = "1332";
		}
		else if(id == 1517)
		{
			icon = "1499";
		}
		else if(id == 1518)
		{
			icon = "1502";
		}
		else
		{
			icon = skillId;
		}
		String name = SkillTable.getInstance().getInfo(id, level).getName();
		name = name.replace("Dance of the", "D.");
		name = name.replace("Dance of", "D.");
		name = name.replace("Song of", "S.");
		name = name.replace("Improved", "I.");
		name = name.replace("Awakening", "A.");
		name = name.replace("Blessing", "Bless.");
		name = name.replace("Protection", "Protect.");
		name = name.replace("Critical", "C.");
		name = name.replace("Condition", "Con.");
		sb.append("<tr><td><center><img src=icon.skill").append(icon).append(" width=32 height=32><br><font color=F2C202>Level ").append(level).append("</font></center></td></tr>");
		sb.append(GenerateElement.buttonTR(name, "_bbscastbuff:" + id + ":" + level + ":" + page + ":" + type, 100, 25, "L2UI_CT1.Button_DF_Down", "L2UI_CT1.Button_DF"));
		sb.append("</table>");
		return sb.toString();
	}

	private static void buff(int id, int level, Playable playable)
	{
		if(id < 20)
			return;

		Skill skill = SkillTable.getInstance().getInfo(id, level > 0 ? level : SkillTable.getInstance().getMaxLevel(id));
		skill.getEffects(playable, playable, false, false, Config.BBS_BUFFER_ALT_TIME * 60000, 0, false);
	}
}