package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.model.Player;
import jts.gameserver.scripts.Functions;
import jts.gameserver.model.base.Experience;

public class lvl extends Functions
{
	private int SubMaxLvlUp = Config.SERVICES_LVL_UP_MAX >= 80 ? 80 : Config.SERVICES_LVL_UP_MAX;
	
	public void list()
	{
		Player player = getSelf();
		if(!Config.SERVICES_LVL_ENABLED)
		{
			player.sendMessage("Данный сервис недоступен.");
			return;
		}
		if(player.isSubClassActive())
		{
			if(player.getLevel() >= SubMaxLvlUp)
			{
				player.sendMessage("Ваш уровень не соответствует условиям пользования сервисом.");
				return;
			}
		}
		else
		{
			if(player.getLevel() >= Config.SERVICES_LVL_UP_MAX)
			{
				player.sendMessage("Ваш уровень не соответствует условиям пользования сервисом.");
				return;
			}
		}
	}
	
	public void up()
	{
		Player player = getSelf();
		if(player == null)
			return;
		if(!Config.SERVICES_LVL_ENABLED)
		{
			player.sendMessage("Данный сервис недоступен.");
			return;
		}
		int level = player.getLevel()+1;
		if ((Functions.getItemCount(player, Config.SERVICES_LVL_UP_ITEM) >= Config.SERVICES_LVL_UP_PRICE))
		{
			Functions.removeItem(player, Config.SERVICES_LVL_UP_ITEM, Config.SERVICES_LVL_UP_PRICE);
                        setLevel(player, level);
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
	}
        
	private void setLevel(Player player, int level)
	{
		Long exp_add = Experience.LEVEL[level] - player.getExp();
		player.addExpAndSp(exp_add, 0, 0, 0, false, false);
		if(player.isSubClassActive())
		{
			if(player.getLevel() >= SubMaxLvlUp)
			{
				player.sendMessage("Ваш уровень не соответствует условиям пользования сервисом.");
				return;
			}
		}
		else
		{
			if(player.getLevel() >= Config.SERVICES_LVL_UP_MAX)
			{
				player.sendMessage("Ваш уровень не соответствует условиям пользования сервисом.");
				return;
			}
		}
		
		player.sendMessage("Поздравляем Вы повысили свой уровень.");
		return;
	}
}