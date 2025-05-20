package jts.gameserver.xmrpcserver.XMLServices;

import jts.gameserver.Config;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.xmrpcserver.XMLUtils;
import jts.gameserver.xmrpcserver.model.Message.MessageType;
import jts.gameserver.xmrpcserver.model.ServerRates;

public class WorldService extends Base
{
	/**
	 * Пустой метод для запроса на сервер, чтобы понять, запущен он или нет.
	 * @return
	 */
	public String idle()
	{
		return json(MessageType.OK);
	}

	/**
	 * @return количество игроков онлайн
	 */
	public String getOnlinePlayersCount()
	{
		return json(String.valueOf(GameObjectsStorage.getAllPlayersCount()));
	}

	/**
	 * Список рейтов сервера.
	 * @return
	 */
	public String getRates()
	{
		return json(new ServerRates(Config.RATE_XP, Config.RATE_SP, Config.RATE_QUESTS_REWARD, Config.RATE_QUESTS_DROP, Config.RATE_DROP_ITEMS, Config.RATE_DROP_SPOIL, Config.RATE_DROP_RAIDBOSS));
	}

	/**
	 * @return сериализованные инстансы игроков онлайн
	 */
	public String getAllOnlinePlayersInfo()
	{
		StringBuilder result = new StringBuilder();
		result.append("");
		result.append("<charlist>");
		for(Player pc : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(pc.isVisible())
			{
				result.append(XMLUtils.serializePlayer(pc, false));
			}
		}
		result.append("</charlist>");
		return json(result);
	}
}
