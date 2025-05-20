package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.Config;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.database.mysql;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.AutoBan;
// © Тиханов 30.06.2015
public class UnBans extends Functions implements ScriptFile
{

	public void unbans_page()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(!Config.ServicesUnBan)
		{
			player.sendMessage("Сервис отключен.");
			return;
		}
		String append = "Сервисы снятия бана.<br>";
		append += "<button value=\"Разбанить аккаунт\" action=\"bypass -h scripts_services.UnBans:unban_page_acc\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		append += "<button value=\"Разбанить персонажа\" action=\"bypass -h scripts_services.UnBans:unban_page_char\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		append += "<button value=\"Разбанить чат\" action=\"bypass -h scripts_services.UnBans:unban_page_chat\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		show(append, player, null);
	}
	
	public void unban_page_acc()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(!Config.ServicesUnBanAcc)
		{
			player.sendMessage("Сервис отключен.");
			return;
		}
		String append = "Сервис снятия бана c аккаунта.<br>";
		append += "Цена: " + Config.ServicesUnBanAccCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanAccItem).getName() +".<br>";
		append += "Введите название аккаунта: <edit var=\"acc\" width=80><br>";
		append += "<button value=\"Снять бан\" action=\"bypass -h scripts_services.UnBans:unban_acc $acc\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		show(append, player, null);
	}
	
	public void unban_acc(String[] name)
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(player.getInventory().getCountOf(Config.ServicesUnBanAccItem) < Config.ServicesUnBanAccCount)
		{
			player.sendMessage("У вас нету " + Config.ServicesUnBanAccCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanAccItem).getName());
			return;
		}
		if(name.length < 1 || name.length > 16)
		{
			player.sendMessage("Не коректное название");
			return;
		}
		String _name = name[0];
		Connection con = null;
		Connection conLS = null;
		PreparedStatement offline = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT login FROM `" + Config.LOGIN_DB + "`.`accounts` WHERE login = ?");
			offline.setString(1, _name);
			rs = offline.executeQuery();
			if(rs.next())
			{
				mysql.set("UPDATE `" + Config.LOGIN_DB + "`.`accounts` SET `ban_expire` = '0', `access_level` = '0' WHERE `login` = '" + _name + "'");
				removeItem(player, Config.ServicesUnBanAccItem, Config.ServicesUnBanAccCount);
				unban_acc_ok();
			}
			else
			{
				player.sendMessage("Введенный аккаунт не найден.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
			DbUtils.closeQuietly(conLS, statement);
		}
	}


	public void unban_acc_ok()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		String append = "Сервис снятия бана c аккаунта.<br>";
		append += "Аккаунт успешно разбанен.<br>";
		show(append, player, null);
	}
	public void unban_page_char()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(!Config.ServicesUnBanChar)
		{
			player.sendMessage("Сервис отключен.");
			return;
		}
		String append = "Сервис снятия бана c персонажа.<br>";
		append += "Цена: " + Config.ServicesUnBanCharCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanCharItem).getName() +".<br>";
		append += "Введите имя персонажа: <edit var=\"char\" width=80><br>";
		append += "<button value=\"Снять бан\" action=\"bypass -h scripts_services.UnBans:unban_char $char\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		show(append, player, null);
	}
	public void unban_char(String[] name)
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(player.getInventory().getCountOf(Config.ServicesUnBanCharItem) < Config.ServicesUnBanCharCount)
		{
			player.sendMessage("У вас нету " + Config.ServicesUnBanCharCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanCharItem).getName());
			return;
		}
		if(name.length < 1 || name.length > 16)
		{
			player.sendMessage("Не коректное имя");
			return;
		}
		String _name = name[0];
		Connection con = null;
		Connection conLS = null;
		PreparedStatement offline = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `char_name` = ?");
			offline.setString(1, _name);
			rs = offline.executeQuery();
			if(rs.next())
			{
				mysql.set("UPDATE `characters` SET `accesslevel` = '0' WHERE `char_name` = '" + _name + "'");
				removeItem(player, Config.ServicesUnBanCharItem, Config.ServicesUnBanCharCount);
				unban_char_ok();
			}
			else
			{
				player.sendMessage("Введенный ник персонажа не найден.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, offline, rs);
			DbUtils.closeQuietly(conLS, statement);
		}
    	}
	public void unban_page_chat()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(!Config.ServicesUnBanChat)
		{
			player.sendMessage("Сервис отключен.");
			return;
		}
		String append = "Сервис снятия бана c чата.<br>";
		append += "Цена: " + Config.ServicesUnBanChatCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanChatItem).getName() +".<br>";
		append += "Введите имя персонажа с которого снять бан чата: <edit var=\"chat\" width=80><br>";
		append += "<button value=\"Снять бан\" action=\"bypass -h scripts_services.UnBans:unban_chat $chat\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		show(append, player, null);
	}

	public void unban_chat(String[] name)
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(player.getInventory().getCountOf(Config.ServicesUnBanChatItem) < Config.ServicesUnBanChatCount)
		{
			player.sendMessage("У вас нету " + Config.ServicesUnBanChatCount + " " + ItemHolder.getInstance().getTemplate(Config.ServicesUnBanChatItem).getName());
			return;
		}
		else
			removeItem(player, Config.ServicesUnBanChatItem, Config.ServicesUnBanChatCount);
		if(name.length < 1 || name.length > 16)
		{
			player.sendMessage("Не коректное название");
			return;
		}

		String _name = name[0];
		AutoBan.ChatUnBan(_name, _name);
		unban_chat_ok();
    	}

	public void unban_chat_ok()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		String append = "Сервис снятия бана c чата.<br>";
		append += "Чат успешно разбанен.<br>";
		show(append, player, null);
	}
	public void unban_char_ok()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		String append = "Сервис снятия бана c персонажа.<br>";
		append += "Персонаж успешно разбанен.<br>";
		show(append, player, null);
	}
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}