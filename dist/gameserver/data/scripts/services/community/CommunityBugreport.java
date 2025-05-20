package services.community;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import jts.gameserver.Config;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.tables.GmListTable;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityBugreport extends Functions implements ScriptFile, ICommunityBoardHandler
{

    static final Logger _log = LoggerFactory.getLogger(CommunityBugreport.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public void onLoad() {
        {
        }
        CommunityBoardManager.getInstance().registerHandler(this);
    }

    @Override
    public void onReload() {
        {
            CommunityBoardManager.getInstance().removeHandler(this);
        }
    }

    @Override
    public void onShutdown() 
    {

    }

    @Override
    public String[] getBypassCommands()
    {
        return new String[]{"_bbsbugreport"};
    }

    @Override
    public void onBypassCommand(Player player, String bypass) 
    {
        if (bypass.startsWith("_bbsbugreport")) 
        {
            String args[] = bypass.split(":");
            String message = "";
            String _type = null;
            String info = player.getName();
            String info1 = player.getTemplate().className;
            String info2 = String.valueOf(player.getLevel());
            String info3 = String.valueOf(player.getIP());
            String info4 = String.valueOf(player.getHWID());
            String info5 = String.valueOf(player.getX() + "," + player.getY() + "," + player.getZ() + "," + player.getHeading());
            String info6 = player.getAccountName();
            String date = DATE_FORMAT.format(new Timestamp(System.currentTimeMillis()));

            try 
            {
                _type = String.valueOf(args[1]);
                message = message + String.valueOf(args[2]) + " ";

                if (message.equals(""))
                {
                    player.sendMessage(
                            player.isLangRus() ? "Сообщение не может быть пустым." : "Message box cannot be empty.");
                    return;
                }
                File f = new File("./bugreports/");
                if (!f.exists()) {
                    new File("./bugreports/").mkdir();
                }

                String fname = "bugreports/" + date + "_" + player.getName() + ".txt";
                File file = new File(Config.DATAPACK_ROOT, fname);
                boolean exist = file.createNewFile();
                if (!exist) {
                    player.sendMessage(player.isLangRus()
                            ? "Вы уже отправили сообщение о ошибке, Администрация должна сначала его проверить."
                            : "You have already sent a bug report, GMs must check it first.");
                    return;
                }

                FileWriter fstream = new FileWriter(fname);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("Информация о игроке: " + info + "\r\nАккаунт: " + info6 + "\r\nПрофа: " + info1 + "\r\nУровень: " + info2 + "\r\nIP: " + info3 + "\r\nHWID: " + info4 + "\r\nЛокация: " + info5 
                        + "\r\nТип ошибки: " + _type + "\r\nСообщение: " + message);
                player.sendMessage(player.isLangRus()
                        ? "Сообщение отправлено. Администрация проверит в ближайшее время. Спасибо..."
                        : "Report sent. GMs will check it soon. Thanks...");

                Say2 cs = new Say2(0, ChatType.SHOUT, "Менеджер багрепортов", player.getName() + " сообщил об ошибке.");
                GmListTable.broadcastToGMs(cs);
                out.close();
            } catch (Exception e) {
                player.sendMessage(player.isLangRus() ? "Что-то пошло не так, попробуйте ещё раз."
                        : "Something went wrong try again.");
            }
        }
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4,
            String arg5) {

    }

}
