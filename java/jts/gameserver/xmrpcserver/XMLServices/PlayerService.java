package jts.gameserver.xmrpcserver.XMLServices;

import javolution.util.FastList;
import jts.gameserver.dao.CharacterDAO;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.utils.ItemFunctions;
import jts.gameserver.utils.Util;
import jts.gameserver.xmrpcserver.XMLUtils;
import jts.gameserver.xmrpcserver.model.Message.MessageType;
import org.apache.log4j.Level;

import java.util.List;

public class PlayerService extends Base
{
	public static final String SELECT_CHAR_ACCOUNT = "SELECT char_name FROM characters WHERE account_name=?";

	/**
	 * Добавляет персонажу заданный итем
	 * @param playerName имя игрока
	 * @param itemId ID предмета
	 * @param count количество предмета
	 * @return {@code OK} если добавление состоялось удачно, {@code FAIL} если по каким-то причинам добавление не состоялось
	 */
	public String addItemToPlayer(String playerName, int itemId, int count)
	{
		ItemInstance item = ItemFunctions.createItem(itemId);
		try
		{
			Player player = World.getPlayer(playerName);
			int objId = CharacterDAO.getInstance().getObjectIdByName(playerName);
			int playerId = Integer.parseInt(CharacterDAO.getInstance().getNameByObjectId(objId));
			if(playerId < 0)
			{
				return json(MessageType.FAILED);
			}
			if(player == null)
			{
				item.setOwnerId(playerId);
				item.setLocation(ItemInstance.ItemLocation.INVENTORY);
				World.removeVisibleObject(item);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + playerName + " donated ItemId: " + itemId + " Count: " + count + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.getInventory().addItem(item);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + playerName + " donated ItemId: " + itemId + " Count: " + count + " [IP: " + /*player.getClient().getConnectionAddress().getHostAddress()*/ " HWID: " /*(player.getClient().getHWID() != null ? player.getClient().getHWID() : "NONE")*/ + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Устанавливает игроку указанный цвет ника
	 * @param charName имя игрока
	 * @param color цвет в RGB
	 * @return результат операции
	 */
	public String setNameColor(String charName, int color)
	{
		try
		{
			Player player = World.getPlayer(charName);
			if(player == null)
			{
				conn = DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("UPDATE characters SET name_color=? WHERE char_name=?");
				statement.setInt(1, color);
				statement.setString(2, charName);
				statement.execute();
				databaseClose(false);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed name color to " + Integer.toHexString(color) + " RGB" + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.setNameColor(color);
				player.broadcastUserInfo(true);
				player.store(true);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed name color to " + Integer.toHexString(color) + " RGB" + " [IP: " + " HWID: " + "NONE" + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Устанавливает игроку указанный цвет титула
	 * @param charName имя игрока
	 * @param color цвет в RGB
	 * @return результат операции
	 */
	public String setTitleColor(String charName, int color)
	{
		try
		{
			Player player = World.getPlayer(charName);
			if(player == null)
			{
				conn = DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("UPDATE characters SET title_color=? WHERE char_name=?");
				statement.setInt(1, color);
				statement.setString(2, charName);
				statement.execute();
				databaseClose(false);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " title name color to " + Integer.toHexString(color) + " RGB" + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.setTitleColor(color);
				player.broadcastUserInfo(true);
				player.store(true);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed title color to " + Integer.toHexString(color) + " RGB" + " [IP: "  + " HWID: " + "NONE" + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Сброс кармы игрока, если она у него отрицательная
	 * @param charName имя игрока
	 * @return результат операции
	 */
	public String resetReputationToZero(String charName)
	{
		try
		{
			Player player = World.getPlayer(charName);
			if(player == null)
			{
				int currentReputation = 0;

				conn = DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("SELECT karma FROM characters WHERE char_name=?");
				statement.setString(1, charName);
				resultSet = statement.executeQuery();
				while(resultSet.next())
				{
					currentReputation = resultSet.getInt("reputation");
				}

				if(currentReputation < 0)
				{
					statement = conn.prepareStatement("UPDATE characters SET karma=0 WHERE char_name=?");
					statement.setString(1, charName);
					statement.execute();
					databaseClose(true);
					return json(MessageType.OK);
				}
				else
				{
					databaseClose(true);
					return json(MessageType.FAILED);
				}
			}
			else
			{
				int currentReputation = player.getKarma();
				if(currentReputation < 0)
				{
					player.setKarma(0);
					return json(MessageType.OK);
				}
				else
				{
					return json(MessageType.FAILED);
				}
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Возвращает всех персонажей с заданным аккаунтом
	 * @param account имя аккаунта
	 * @return сериализованные инстансы игроков на аккаунте
	 */
	public String getAllCharsFromAccount(String account)
	{
		StringBuilder result = new StringBuilder();
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(SELECT_CHAR_ACCOUNT);
			statement.setString(1, account);
			resultSet = statement.executeQuery();
			Player pc;
			while(resultSet.next())
			{
				pc = Util.loadPlayer(resultSet.getString(1), true);
				if(pc != null)
				{
					result.append(XMLUtils.serializePlayer(pc, true));
				}
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
		finally
		{
			databaseClose(true);
		}
		result.append("</templates>");
		return json(result.toString());
	}

	/**
	 * Возвращает список имен персонажей аккаунта.
	 * @param account имя аккаунта
	 * @return игроков на аккаунте
	 */
	public String listCharacterNames(String account)
	{
		List<String> names = new FastList<>();
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(SELECT_CHAR_ACCOUNT);
			statement.setString(1, account);
			resultSet = statement.executeQuery();
			while(resultSet.next())
			{
				names.add(resultSet.getString(1));
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
		finally
		{
			databaseClose(true);
		}

		return json(names);
	}

	/**
	 * Сериализует персонажа с указанным именем
	 * @param charName имя персонажа
	 * @param full режим сериализации
	 * @return сериализованный инстанс игрока
	 */
	public String getPlayer(String charName, String full)
	{
		Player pc;
		String result = "";
		pc = World.getPlayer(charName) == null ? Util.loadPlayer(charName, true) : World.getPlayer(charName);

		try
		{
			result += pc != null ? XMLUtils.serializePlayer(pc, Boolean.parseBoolean(full)) : "<char/>";
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, getClass().getSimpleName() + ": Error while getPlayer() : ", e);
		}
		finally
		{
			try
			{
                if (pc != null)
                {
                    pc.deleteMe();
                }
            }
			catch(NullPointerException e)
			{
				log.log(Level.ERROR, getClass().getSimpleName() + ": NPE Error while getPlayer().deleteMe() : ", e);
			}
		}

		return json(result);
	}

}