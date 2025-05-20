package jts.gameserver.handler.voicecommands.impl;

import jts.commons.dao.JdbcEntityState;
import jts.commons.dbutils.DbUtils;
import jts.gameserver.dao.ItemsDAO;
import jts.gameserver.database.DatabaseFactory;
import jts.gameserver.handler.voicecommands.IVoicedCommandHandler;
import jts.gameserver.model.Player;
import jts.gameserver.model.World;
import jts.gameserver.model.items.ItemInstance;
import jts.gameserver.model.items.ItemInstance.ItemLocation;
import jts.gameserver.network.serverpackets.components.CustomMessage;
import jts.gameserver.scripts.Functions;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public class Repair extends Functions implements IVoicedCommandHandler 
{

    private static final Logger _log = LoggerFactory.getLogger(Repair.class);
    private final String[] _commandList = new String[]{"repair"};

    @Override
    public String[] getVoicedCommandList()
    {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String target)
    {
        if (!target.isEmpty()) 
        {
            if (activeChar.getName().equalsIgnoreCase(target)) 
            {
                sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCantRepairYourself", activeChar),
                        activeChar);
                return false;
            }

            int objId = 0;

            for (Map.Entry<Integer, String> e : activeChar.getAccountChars().entrySet())
            {
                if (e.getValue().equalsIgnoreCase(target))
                {
                    objId = e.getKey();
                    break;
                }
            }

            if (objId == 0)
            {
                sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCanRepairOnlyOnSameAccount", activeChar),
                        activeChar);
                return false;
            } 
            else if (World.getPlayer(objId) != null)
            {
                sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsOnline", activeChar), activeChar);
                return false;
            }

            Connection con = null;
            PreparedStatement statement = null;
            ResultSet rs = null;
            int karma = 0;
            int jail = 0;
            try 
            {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT karma FROM characters WHERE obj_Id=?");
                statement.setInt(1, objId);
                statement.execute();
                rs = statement.getResultSet();

                rs.next();
                karma = rs.getInt("karma");
                DbUtils.close(statement, rs);

                statement = con
                        .prepareStatement("SELECT `value` FROM `character_variables` WHERE `obj_Id`=? AND `name`=?");
                statement.setInt(1, objId);
                statement.setString(2, "jailed");
                statement.execute();
                rs = statement.getResultSet();

                while (rs.next()) 
                {
                    jail = rs.getInt("value");
                }

                DbUtils.close(statement, rs);

                if (jail != 0) 
                {
                    sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsJail", activeChar), activeChar);
                    return false;
                }

                if (karma > 0) 
                {
                    statement = con.prepareStatement("UPDATE characters SET x=82504, y=148536, z=-3494 WHERE obj_Id=?");
                    statement.setInt(1, objId);
                    statement.execute();
                    DbUtils.close(statement);
                } 
                else 
                {
                    statement = con.prepareStatement("UPDATE characters SET x=0, y=0, z=0 WHERE obj_Id=?");
                    statement.setInt(1, objId);
                    statement.execute();
                    DbUtils.close(statement);

                    Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId,
                            ItemLocation.PAPERDOLL);
                    for (ItemInstance item : items)
                    {
                        item.setEquipped(false);
                        if (item.isHeroWeapon()) // хиро оружие не должно
                        // ложится в вх, так как
                        // возможен дюп
                        {
                            continue;
                        }
                        item.setLocData(0);
                        item.setLocation(ItemLocation.WAREHOUSE);
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();
                    }
                }

                statement = con.prepareStatement(
                        "DELETE FROM character_variables WHERE obj_id=? AND type='user-var' AND name='reflection'");
                statement.setInt(1, objId);
                statement.execute();
                DbUtils.close(statement);

				if (activeChar.isLangRus())
				{
					show("Персонаж восстановлен в ближайший Город, Все вещи с персонажа в приватном Банке.", activeChar);
				}
				else
				{
					show("Character restored in the nearest town, all things with the character in a private bank.", activeChar);
				}
                return true;
            }
            catch (Exception e)
            {
                _log.error(StringUtils.EMPTY, e);
                return false;
            } 
            finally 
            {
                DbUtils.closeQuietly(con, statement, rs);
            }
        } 
        else 
			
		if (activeChar.isLangRus())
		{
			show("Напишите в чате комманду .repair и Имя персонажа , пример: .repair Server", activeChar);
		}
		else
		{
			show("Write chatting Commando .repair and Player name , example: .repair Server", activeChar);
		}
		return false;
	}
}
