package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.xml.holder.ItemHolder;
import jts.gameserver.model.Player;
import jts.gameserver.model.pledge.Clan;
import jts.gameserver.model.pledge.UnitMember;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.network.serverpackets.MagicSkillUse;
import jts.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import jts.gameserver.network.serverpackets.PledgeStatusChanged;
import jts.gameserver.scripts.Functions;
import jts.gameserver.utils.SiegeUtils;

public class BuyClanLvL extends Functions
{

	public void list()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(player.getClan() == null)
		{
			player.sendMessage("Нельзя использовать сервис если нет клана");
			return;
		}
		if(player.getClan().getLeader().getPlayer() != player)
		{
			player.sendMessage("Нельзя использовать сервис если вы не клан лидер");
			return;
		}
		int clanlevel = player.getClan().getLevel();
		StringBuilder sb = new StringBuilder();
		sb.append("<font color=\"LEVEL\">Сервис продажи Уровня Клана</font><br>");
 	//	int result = 0;
		int value = 0;
		if(clanlevel >= 11)
		{
			sb.append("<tr>");
			sb.append("<font color=\"LEVEL\">Уровень Клана Максимальный</font><br>");
			sb.append("</tr>");
			
			
		}
		else
    	for (int i = 0; i < Config.SERVICES_CLANLVL_PRICE.length/ 3; i++) 
    	{ 
    		value+=3;
    		if(clanlevel +1 == Config.SERVICES_CLANLVL_PRICE[value -1])
    		{
    			String pricename = ItemHolder.getInstance().getTemplate(Config.SERVICES_CLANLVL_PRICE[value-3]).getName();
    			sb.append("<font color=\"LEVEL\">Текущий Уровень Клана </font> " + clanlevel + "<br>");
    			sb.append("<tr>"); 		
    			sb.append("Повысить уровень Клана За " + Config.SERVICES_CLANLVL_PRICE[value-2] + " "+ pricename +"");
    			sb.append("<button value=\"Повысить\" action=\"bypass -h scripts_services.BuyClanLvL:get "+ i + "\" width=60 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
    			sb.append("<br1>");
    			sb.append("</tr>");
    			
    		}
    		//result++;
    	}
		show(sb.toString(), player, null);
	}
	private int[] getPrice(int s, int[] massive)
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
    				massive[value-3],//цена
    				massive[value-2],//количество
    				massive[value-1],//уровень клана
    			};
    			
    		}
    		result ++ ;
    	}		
		return z;
	}
	
	public void get(String[] param)
	{
		
		int pars = Integer.parseInt(param[0]);
		Player player = getSelf();
		if(!Config.SERVICES_CLANLVL_ACTIVE)
		{
			player.sendMessage("Сервис не работает");
			return;
		}	
		if(player.getClan() == null)
		{
			player.sendMessage("Нельзя использовать сервис если нет клана");
			return;
		}
		if(player.getClan().getLeader().getPlayer() != player)
		{
			player.sendMessage("Нельзя использовать сервис если вы не клан лидер");
			return;
		}
		int clanlevel = player.getClan().getLevel();
		
		if(clanlevel >= 11)
		{
			player.sendMessage("Нельзя использовать сервис клан больше 11 уровня");
			return;
		}
		int price = getPrice( pars, Config.SERVICES_CLANLVL_PRICE)[0];
		int count = getPrice( pars, Config.SERVICES_CLANLVL_PRICE)[1];
		
		
		if(player.getInventory().destroyItemByItemId(price, count))
		{
			
			addClanLevel(player);
		}
		else if(price == 57)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
		}
		else
		{
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
		}
		list();
	}

public void addClanLevel(Player player)
{
	Clan clan = player.getClan();
	clan.setLevel(clan.getLevel() + 1);
	clan.updateClanInDB();

	player.broadcastCharInfo();
	player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 1, 0));
	if(clan.getLevel() >= 4)
		SiegeUtils.addSiegeSkills(player);

	if(clan.getLevel() == 5)
		player.sendPacket(SystemMsg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);

	// notify all the members about it
	PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
	PledgeStatusChanged ps = new PledgeStatusChanged(clan);
	for(UnitMember mbr : clan)
		if(mbr.isOnline())
		{
			mbr.getPlayer().updatePledgeClass();
			mbr.getPlayer().sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
			mbr.getPlayer().broadcastCharInfo();
		}
	
}
}

