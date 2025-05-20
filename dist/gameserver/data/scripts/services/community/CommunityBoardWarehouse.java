package services.community;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ShowBoard;
import jts.gameserver.scripts.ScriptFile;
import jts.gameserver.utils.WarehouseFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

public class CommunityBoardWarehouse implements ScriptFile, ICommunityBoardHandler {
    private static final Logger _log = LoggerFactory.getLogger(CommunityBoardWarehouse.class);

    @Override
    public String[] getBypassCommands() 
	{
        return new String[]{
                "_bbswarehouse",
        };
    }

    @Override
    public void onBypassCommand(Player player, String bypass) 
	{
        if(!Config.ALLOW_BBS_WAREHOUSE)
		{
			player.sendMessage(player.isLangRus() ? "Склад Отключен" : "Warehouse offline");
			return;
		}
        if (player == null) return;
		if(!Config.BBS_WAREHOUSE_ALLOW_PEACE_ZONE)
		if (!player.isInPeaceZone())
		{
            player.sendMessage(player.isLangRus() ? "Запрещено использовать склад за пределами города" : "You may not use a warehouse outside the city");
			return;
		}
        if (!Config.BBS_WAREHOUSE_ALLOW_PK && player.getKarma() > 0) 
		{
            player.sendMessage(player.isLangRus() ? "PK нельзя использовать склад" : "PK can not use a warehouse");
            return;
        }
		if(player.isInTrade())
		{
            player.sendMessage(player.isLangRus() ? "Запрещено использовать склад пока вы торгуете" : "You may not use stock while you are trading");
			return;
		}

		if(player.isFishing())
		{
            player.sendMessage(player.isLangRus() ? "Запрещено использовать склад пока вы ловите рыбу" : "You may not use stock while you are fishing");
			return;
		}
		if(player.getEnchantScroll() != null)
		{
            player.sendMessage(player.isLangRus() ? "Запрещено использовать склад пока вы затачиваете предмет" : "You may not use stock until you hone the subject");
			return;
		}
        StringTokenizer st = new StringTokenizer(bypass, ":");
        st.nextToken();
        String action = st.hasMoreTokens() ? st.nextToken() : "";
        if (action.equalsIgnoreCase("private_deposit"))
            WarehouseFunctions.showDepositWindow(player);
        else if (action.equalsIgnoreCase("private_retrieve"))
            WarehouseFunctions.showRetrieveWindow(player, getVal(st.nextToken()));
        else if (action.equalsIgnoreCase("clan_deposit"))
            WarehouseFunctions.showDepositWindowClan(player);
        else if (action.equalsIgnoreCase("clan_retrieve"))
            WarehouseFunctions.showWithdrawWindowClan(player, getVal(st.nextToken()));
        showMain(player);
    }
    
    private int getVal(String name) 
	{
        name = name.trim();
        if (name.equalsIgnoreCase("Оружие") || name.equalsIgnoreCase("weapon") || name.equalsIgnoreCase("1"))
            return 1;
        else if (name.equalsIgnoreCase("Броня") || name.equalsIgnoreCase("armor") || name.equalsIgnoreCase("2"))
            return 2;
        else if (name.equalsIgnoreCase("Бижутерия") || name.equalsIgnoreCase("jewelry") || name.equalsIgnoreCase("3"))
            return 3;
        else if (name.equalsIgnoreCase("Украшения") || name.equalsIgnoreCase("ornamentation") || name.equalsIgnoreCase("4"))
            return 4;
        else if (name.equalsIgnoreCase("Предметы снабжения") || name.equalsIgnoreCase("supplies") || name.equalsIgnoreCase("5"))
            return 5;
        else if (name.equalsIgnoreCase("Материалы") || name.equalsIgnoreCase("materials") || name.equalsIgnoreCase("6"))
            return 6;
        else if (name.equalsIgnoreCase("Ключевые материалы") || name.equalsIgnoreCase("key materials") || name.equalsIgnoreCase("7"))
            return 7;
        else if (name.equalsIgnoreCase("Рецепты") || name.equalsIgnoreCase("recipes") || name.equalsIgnoreCase("8"))
            return 8;
        else if (name.equalsIgnoreCase("Книги") || name.equalsIgnoreCase("books") || name.equalsIgnoreCase("9"))
            return 9;
        else if (name.equalsIgnoreCase("Разное") || name.equalsIgnoreCase("Miscellaneous") || name.equalsIgnoreCase("10"))
            return 10;
        else if (name.equalsIgnoreCase("Прочее") || name.equalsIgnoreCase("Other") || name.equalsIgnoreCase("11"))
            return 11;

        return 0;
    }
    
    private void showMain(Player player) 
	{
        if (player == null) return;
        String htm = HtmCache.getInstance().getNotNull("scripts/services/community/high/pages/warehouse.htm", player);
        StringBuilder sb = new StringBuilder();
        htm = htm.replace("<?content?>", sb.toString());
        ShowBoard.separateAndSend(htm, player);
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {}

    @Override
    public void onLoad() 
	{
        if (Config.COMMUNITYBOARD_ENABLED) {
            _log.info("CommunityBoard: Warehouse loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public void onReload() 
	{
        if (Config.COMMUNITYBOARD_ENABLED)
            CommunityBoardManager.getInstance().removeHandler(this);
    }
	
    @Override
    public void onShutdown() {}
}