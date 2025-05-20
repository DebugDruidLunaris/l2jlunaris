package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jts.commons.dbutils.DbUtils;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

public class Account extends Functions implements ScriptFile
{
	private static int ACC_MOVE_ITEM = 57;
	private static int ACC_MOVE_PRICE = 1000;

	public void CharToAcc()
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		String append = "Перенос персонажей между аккаунтами.<br>";
		append += "Цена: " + ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(ACC_MOVE_ITEM).getName() + ".<br>";
		append += "Внимание !!! При переносе персонажа на другой аккаунт, убедитесь что персонажей там меньше чем 7, иначе могут возникнуть непредвиденные ситуации за которые Администрация не отвечает.<br>";
		append += "Внимательно вводите логин куда переносите, администрация не возвращает персонажей.";
		append += "Вы переносите персонажа " + player.getName() + ", на какой аккаунт его перенести ?";
		append += "<edit var=\"new_acc\" width=150>";
		append += "<button value=\"Перенести\" action=\"bypass -h scripts_services.Account:NewAccount $new_acc\" back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\" width=\"150\" height=\"15\"><br>";
		show(append, player, null);
	}

	public void NewAccount(String[] name)
	{
		Player player = (Player) getSelf();
		if(player == null)
			return;
		if(player.getInventory().getCountOf(ACC_MOVE_ITEM) < ACC_MOVE_PRICE)
		{
			player.sendMessage("У вас нету " + ACC_MOVE_PRICE + " " + ItemHolder.getInstance().getTemplate(ACC_MOVE_ITEM));
			CharToAcc();
			return;
		}
		String _name = name[0];
		Connection con = null;
		Connection conGS = null;
		PreparedStatement offline = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT `login` FROM `accounts` WHERE `login` = ?");
			offline.setString(1, _name);
			rs = offline.executeQuery();
			if(rs.next())
			{
				removeItem(player, ACC_MOVE_ITEM, ACC_MOVE_PRICE);
				conGS = DatabaseFactory.getInstance().getConnection();
				statement = conGS.createStatement();
				statement.executeUpdate("UPDATE `characters` SET `account_name` = '" + _name + "' WHERE `char_name` = '" + player.getName() + "'");
				player.sendMessage("Персонаж успешно перенесен.");
				player.logout();
			}
			else
			{
				player.sendMessage("Введенный аккаунт не найден.");
				CharToAcc();
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
			DbUtils.closeQuietly(conGS, statement);
		}
	}

	public void onLoad() {}
	public void onReload() {}
	public void onShutdown() {}
}