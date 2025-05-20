package services.community;

import jts.commons.util.Rnd;
import jts.gameserver.common.DifferentMethods;
import jts.gameserver.loginservercon.LoginServerCommunication;
import jts.gameserver.loginservercon.gspackets.ChangePassword;
import jts.gameserver.model.Player;
import jts.gameserver.model.base.Race;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.components.CustomMessage;

public class CommunityBoardCabinet
{
	private static String _msg;
	private static String OFF = "<font color=\"FF0000\">OFF</font>";
	private static String ON = "<font color=\"00CC00\">ON</font>";

	public static void changePassword(Player player, String old, String newPass1, String newPass2, String number1, String number2, String captcha)
	{
		if(player == null)
			return;

		if(old.equals(newPass1))
		{
			player.setPasswordResult(new CustomMessage("communityboard.cabinet.password.incorrect.newisold", player).toString());
			return;
		}

		if(newPass1.length() < 4 || newPass1.length() > 20)
		{
			player.setPasswordResult(new CustomMessage("communityboard.cabinet.password.incorrect.size", player).toString());
			return;
		}

		if(!newPass1.equals(newPass2))
		{
			player.setPasswordResult(new CustomMessage("communityboard.cabinet.password.incorrect.confirmation", player).toString());
			return;
		}

		if(Integer.valueOf(number1) + Integer.valueOf(number2) != Integer.valueOf(captcha))
		{
			int captchaA = Integer.valueOf(number1) + Integer.valueOf(number2);
			player.setPasswordResult(new CustomMessage("communityboard.cabinet.password.incorrect.captcha", player).addNumber(captchaA).toString());
			return;
		}

		LoginServerCommunication.getInstance().sendPacket(new ChangePassword(player.getAccountName(), old, newPass1));
		return;
	}

	public static int doCaptcha(boolean n1, boolean n2)
	{
		int captcha = 0;
		if(n1)
			captcha = Rnd.get(1, 499);
		if(n2)
			captcha = Rnd.get(1, 499);

		return captcha;
	}

	public static void changeHairStyle(Player player, String race, String id)
	{
		if(player == null || race == null || id == null)
			return;

		boolean check = false;

		boolean HumanMage = player.getRace() == Race.human && player.isMageClass();
		boolean HumanWarrior = player.getRace() == Race.human && !player.isMageClass();

		boolean ElfMage = player.getRace() == Race.elf && player.isMageClass();
		boolean ElfWarrior = player.getRace() == Race.elf && !player.isMageClass();

		boolean DarkElfMage = player.getRace() == Race.darkelf && player.isMageClass();
		boolean DarkElfWarrior = player.getRace() == Race.darkelf && !player.isMageClass();

		boolean OrcMage = player.getRace() == Race.orc && player.isMageClass();
		boolean OrcWarrior = player.getRace() == Race.orc && !player.isMageClass();

		boolean Dwarf = player.getRace() == Race.dwarf;

		boolean Kamael = player.getRace() == Race.kamael;

		if(race.equals("Human-Mage"))
		{
			if(HumanMage)
				check = true;
			else
				player.sendMessage("Эти прически для Human-Mage");
		}
		if(race.equals("Human-Warrior"))
		{
			if(HumanWarrior)
				check = true;
			else
				player.sendMessage("Эти прически для Human-Warrior");
		}
		else if(race.equals("Elf-Mage"))
		{
			if(ElfMage)
				check = true;
			else
				player.sendMessage("Эти прически для Elf-Mage");
		}
		else if(race.equals("Elf-Warrior"))
		{
			if(ElfWarrior)
				check = true;
			else
				player.sendMessage("Эти прически для Elf-Warrior");
		}
		else if(race.equals("DarkElf-Mage"))
		{
			if(DarkElfMage)
				check = true;
			else
				player.sendMessage("Эти прически для DarkElf-Mage");
		}
		else if(race.equals("DarkElf-Warrior"))
		{
			if(DarkElfWarrior)
				check = true;
			else
				player.sendMessage("Эти прически для DarkElf-Warrior");
		}
		else if(race.equals("Orc-Mage"))
		{
			if(OrcMage)
				check = true;
			else
				player.sendMessage("Эти прически для Orc-Mage");
		}
		else if(race.equals("Orc-Warrior"))
		{
			if(OrcWarrior)
				check = true;
			else
				player.sendMessage("Эти прически для Orc-Warrior");
		}
		else if(race.equals("Dwarf"))
		{
			if(Dwarf)
				check = true;
			else
				player.sendMessage("Эти прически для Dwarf");
		}
		else if(race.equals("Kamael"))
		{
			if(Kamael)
				check = true;
			else
				player.sendMessage("Эти прически для Kamael");
		}

		if(check)
		{
			if(DifferentMethods.getPay(player, 57, 100000, true))
			{
				player.setHairStyle(Integer.valueOf(id));
				player.sendMessage("Прическа успешно изменена.");
				player.broadcastPacket(new MagicSkillUse(player, player, 6696, 1, 1000, 0));
				player.broadcastCharInfo();
			}
		}
		return;
	}

	public static String lang(Player player)
	{
		return "<font color=\"339966\">" + player.getVar("lang@") + "</font>";
	}

	public static String DroplistIcons(Player player, boolean button)
	{
		if(button)
		{
			if(player.getVarB("DroplistIcons"))
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:droplisticons:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:droplisticons:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.getVarB("DroplistIcons") ? ON : OFF;
		return _msg;
	}

	public static String NoExp(Player player, boolean button)
	{
		if(button)
		{
			if(player.getVarB("NoExp"))
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:exp:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:exp:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.getVarB("NoExp") ? ON : OFF;
		return _msg;
	}

	public static String NotShowTraders(Player player, boolean button)
	{
		if(button)
		{
			if(player.getVarB("notraders"))
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:notraders:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:notraders:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.getVarB("notraders") ? ON : OFF;
		return _msg;
	}

	public static String notShowBuffAnim(Player player, boolean button)
	{
		if(button)
		{
			if(player.getVarB("notShowBuffAnim"))
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:showbuffanim:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:showbuffanim:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.getVarB("notShowBuffAnim") ? ON : OFF;
		return _msg;
	}

	public static String SkillsHideChance(Player player, boolean button)
	{
		if(button)
		{
			if(player.getVarB("SkillsHideChance"))
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:skillchance:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:skillchance:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.getVarB("SkillsHideChance") ? ON : OFF;
		return _msg;
	}

	public static String AutoLoot(Player player, boolean button)
	{
		if(button)
		{
			if(player.isAutoLootEnabled())
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:autoloot:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:autoloot:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.isAutoLootEnabled() ? ON : OFF;
		return _msg;
	}

	public static String AutoLootHerbs(Player player, boolean button)
	{
		if(button)
		{
			if(player.isAutoLootHerbsEnabled())
				_msg = "<button value=\"OFF\" action=\"bypass _bbscabinet:cfg:autolootherbs:off\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
			else
				_msg = "<button value=\"ON\" action=\"bypass _bbscabinet:cfg:autolootherbs:on\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"35\" height=\"25\"/>";
		}
		else
			_msg = player.isAutoLootHerbsEnabled() ? ON : OFF;
		return _msg;
	}
}