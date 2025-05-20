package services;

import jts.gameserver.Config;
import jts.gameserver.cache.Msg;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.model.Player;
import jts.gameserver.model.entity.olympiad.Olympiad;
import jts.gameserver.network.serverpackets.components.SystemMsg;
import jts.gameserver.scripts.Functions;

public class ResetOlyPoints extends Functions
{

	private static boolean SERVICE_ACTIVE = true; // Включить сервис?
	
	public void list()
	{
		Player player = getSelf();
		if(!Config.SERVICES_OLYMPIAD_RESET_ENABLED)
		{
			player.sendMessage("Данный сервис недоступен.");
			return;
		}
		if(player.isInOlympiadMode() || Olympiad.isRegistered(player))
		{
			player.sendMessage("Недоступно во время участия в олимпиаде.");
			return;
		}
		
		int curPoints = Olympiad.getNoblePoints(player.getObjectId());
		
		String html = HtmCache.getInstance().getNotNull("scripts/services/resetoly.htm", player);
		html = html.replaceFirst("%OlyPointsNow%", player.isNoble() ? "" + curPoints + "" : "нет очков");
		show(html, player);
	}
	
	public void reset()
	{
		Player player = getSelf();
		
		if(!SERVICE_ACTIVE)
		{
			player.sendMessage("Данный сервис недоступен.");
			return;
		}
		if(!player.isNoble())
		{
			player.sendPacket(SystemMsg.THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE);
			return;
		}

		if(player.getInventory().destroyItemByItemId(Config.SERVICES_OLYMPIAD_ITEM, Config.SERVICES_OLYMPIAD_ITEM_PRICE))
		{
			Olympiad.manualSetNoblePoints(player.getObjectId(), 18);
			int newPoints = Olympiad.getNoblePoints(player.getObjectId());
			player.sendMessage("Теперь у Вас: " + newPoints + " очков олимпиады.");
		}
		else
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
		
		int newPoints = Olympiad.getNoblePoints(player.getObjectId());
		String html = HtmCache.getInstance().getNotNull("scripts/services/resetoly.htm", player);
		html = html.replaceFirst("%OlyPointsNow%", player.isNoble() ? "" + newPoints + "" : "нет очков");
		show(html, player);
	}
}