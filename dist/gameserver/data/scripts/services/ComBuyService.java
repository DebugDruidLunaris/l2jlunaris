package services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jts.gameserver.Config;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.handler.bbs.CommunityBoardManager;
import jts.gameserver.handler.bbs.ICommunityBoardHandler;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

/**
 * @author PointerRage
 *
 */
public final class ComBuyService implements ScriptFile, ICommunityBoardHandler { 
{
}
	
	private final static String[] _commands = new String[] {
		"_bbs_1buyfame;", //htm dialog
		"_bbs_buyfame;", 
		"_bbs_1buycrp;", 
		"_bbs_buycrp;", 
		"_bbs_1buyrec;",
		"_bbs_buyrec;"
	};
	private static final Logger _log = LoggerFactory.getLogger(ComBuyService.class);

	@Override
	public void onLoad() {
		
        _log.info("CommunityBoard: Enchant Community service loaded.");
        
        CommunityBoardManager.getInstance().registerHandler(this);
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
		
		return _commands;
	}

	@Override
	public void onBypassCommand(Player player, String bypass) 
	{
		 final String[] commands = bypass.split(";");
		 
		 if(bypass.equalsIgnoreCase("_bbs_1buyfame;"))
		 {
			 if(!Config.SERVICEFAMEACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			 
			 String htm = HtmCache.getInstance().getNotNull("scripts/services/Fame.htm", player);
			 htm = htm.replace("%name%", "Хочешь Приобрести Славы?" );
			 htm = htm.replace("%fame%", "" + getSb(player, " Славы ", "_bbs_buyfame;", Config.SERVICEFAMEPRICE, Config.SERVICEFAMEFREEFORPA ? player.hasBonus() : false));
			 Functions.show(htm, player, null);
		 }
		 else if(bypass.equalsIgnoreCase("_bbs_1buycrp;"))
		 {
			 if(!Config.SERVICECRPACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			 if(player.getClan() == null)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если нет клана.");
				 return;
			 }
			 else if(player.getClan().getLeader().getPlayer() != player)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если вы не клан лидер.");
				 return;
			 }
			 else if(player.getClan().getLevel() < 5)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если клан меньше пятого уровня.");
				 return;
			 }			 
			 String htm = HtmCache.getInstance().getNotNull("scripts/services/CRP.htm", player);
			 htm = htm.replace("%name%", "Хочешь Приобрести CRP?" );
			 htm = htm.replace("%fame%", "" + getSb(player, " CRP ", "_bbs_buycrp;", Config.SERVICECRPPRICE, Config.SERVICECRPFREEFORPA ? player.hasBonus() : false));
			 Functions.show(htm, player, null);
		 }
		 else if(bypass.equalsIgnoreCase("_bbs_1buyrec;"))
		 {
			 if(!Config.SERVICERECOMACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			 String htm = HtmCache.getInstance().getNotNull("scripts/services/rec.htm", player);
			 htm = htm.replace("%name%", "Хочешь Приобрести Рекомендации?" );
			 htm = htm.replace("%fame%", "" + getSb(player, " Реков ", "_bbs_buyrec;" , Config.SERVICERECOMPRICE, Config.SERVICERECOMFREEFORPA ? player.hasBonus() : false));
			 Functions.show(htm, player, null);
		 }
		 else if(commands[0].equalsIgnoreCase("_bbs_buyfame"))
		 {
			 if(!Config.SERVICEFAMEACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			int [] fame = getPriceFame(Integer.parseInt(commands[1]), Config.SERVICEFAMEPRICE);
    		boolean bs = false;
    		if(Config.SERVICEFAMEFREEFORPA)
    		{
    			bs = player.hasBonus();
    		}
 			String htm = HtmCache.getInstance().getNotNull("scripts/services/Thx.htm", player);
 			htm = htm.replace("%back%", "bypass _bbs_1buyfame;" );
			
     		if(bs)
     		{
     			player.setFame(fame[0] + player.getFame(), "game");
     			player.sendMessage("Добавлено очков Славы " + fame[0]);
     			Functions.show(htm, player, null);

     		}
     		else
     			
     		if(player.getInventory().destroyItemByItemId(fame[2], fame[1]))
     		{
     			player.setFame(fame[0] + player.getFame(), "game");
     			player.sendMessage("Добавлено очков Славы " + fame[0]);
     			Functions.show(htm, player, null);
     		}
     		else
     		{
     			player.sendMessage("Не хватает предметов.");
     		} 
		 }
		 else if(commands[0].equalsIgnoreCase("_bbs_buycrp"))
		 {
			 if(!Config.SERVICECRPACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			 if(player.getClan() == null)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если нет клана.");
				 return;
			 }
			 else if(player.getClan().getLeader().getPlayer() != player)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если вы не клан лидер.");
				 return;
			 }
			 else if(player.getClan().getLevel() < 5)
			 {
				 player.sendMessage("Нельзя воспользоваться сервисом если клан меньше пятого уровня.");
				 return;
			 }	
			int [] crp = getPriceFame(Integer.parseInt(commands[1]), Config.SERVICECRPPRICE);
    		boolean bs = false;
    		if(Config.SERVICECRPFREEFORPA)
    		{
    			bs = player.hasBonus();
    		}
 			String htm = HtmCache.getInstance().getNotNull("scripts/services/Thx.htm", player);
 			htm = htm.replace("%back%", "bypass _bbs_1buycrp;" );
			
     		if(bs)
     		{
     			player.getClan().incReputation(crp[0], false, "ClanService" );
     			player.sendMessage("Добавлено очков Клана " + crp[0]);
     			Functions.show(htm, player, null);
     		}
     		else
     			
     		if(player.getInventory().destroyItemByItemId(crp[2], crp[1]))
     		{
     			player.getClan().incReputation(crp[0], false, "ClanService" );
     			player.sendMessage("Добавлено очков Клана " + crp[0]);
     			Functions.show(htm, player, null);
     		}
     		else
     		{
     			player.sendMessage("Не хватает предметов.");
     		} 
		 }
		 else if(commands[0].equalsIgnoreCase("_bbs_buyrec"))
		 {

			 if(!Config.SERVICERECOMACTIVE)
			 {
				 player.sendMessage("Сервис Выключен.");
				 return;
			 }
			int [] rec = getPriceFame(Integer.parseInt(commands[1]), Config.SERVICERECOMPRICE);
    		boolean bs = false;
    		if(Config.SERVICERECOMFREEFORPA)
    		{
    			bs = player.hasBonus();
    		}
			String htm = HtmCache.getInstance().getNotNull("scripts/services/Thx.htm", player);
 			htm = htm.replace("%back%", "bypass _bbs_1buyrec;" );
				
     		if(bs)
     		{
     			
     			player.sendMessage("Добавлено Рекомендаций " + rec[0]);
     			player.setRecomHave(rec[0] + player.getRecomHave());
     			player.broadcastCharInfo();
     			Functions.show(htm, player, null);
     		}
     		else
     			
     		if(player.getInventory().destroyItemByItemId(rec[2], rec[1]))
     		{
     			
     			player.sendMessage("Добавлено Рекомендаций " + rec[0]);
     			player.setRecomHave(rec[0] + player.getRecomHave());
     			player.broadcastCharInfo();
     			Functions.show(htm, player, null);
     		}
     		else
     		{
     			player.sendMessage("Не хватает предметов.");
     		} 
		 }		 
		 
	} 

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
	}
		
	private int[] getPriceFame(int s, int[] massive)
	{
		int[] z = new int[]
		{
			0
		};
		
		int result = 0;
		int value = 0;
    	for (int i = 0; i < massive.length/ 3; i++) 
    	{
    		value+=3;
    		if(result == s)
    		{
    			z = new int[]
    			{
    				massive[value-3],
    				massive[value-2],
    				massive[value-1]
    			};
    			
    		}
    		result ++ ;
    	}		
		return z;
	}

	
	

	 public StringBuilder getSb(Player player, String s, String command, int[] massive, boolean bs)
	 {
	        StringBuilder sb = new StringBuilder();
	        sb.append("<table width=300>");
	        sb.append("<tr>");
	       
	        int result = 0;
	        int value = 0;
	    	for (int i = 0; i < massive.length/ 3; i++) 
	    	{
	    		value+=3;
	    		String priceName = ItemHolder.getInstance().getTemplate(massive[value - 1]).getName();
	    		//количество очков  + за %1(количество) %2 (итемнаме)
	    		//количество очков, коунт, итемид

	    		if(bs)
	    		{
	    			priceName = "Бесплатно"; 
	    		}
	    		sb.append("<tr>");
	    		sb.append("<td><button value=\"Купить " + massive[value - 3] + s + (bs? "" : "за " + massive[value - 2]) + " " +priceName +"\" action=\"bypass "+command+""+result+";" + "\" width=200 height=25 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
	    		sb.append("<br1>");
	    		sb.append("</tr>");
	    		result++;
	    	}
	    	
	    	sb.append("</tr>");
	    	sb.append("</table>");
	    	if(result > 0)
	    	{
	    		return sb;
	    	}
	    	else
	    	{
	    		return null;
	    	}

	 }


	
}
