package jts.gameserver.xmrpcserver.XMLServices;

import jts.gameserver.*;
import jts.gameserver.data.htm.HtmCache;
import jts.gameserver.data.xml.holder.MultiSellHolder;
import jts.gameserver.data.xml.holder.ProductHolder;
import jts.gameserver.data.xml.parser.ItemParser;
import jts.gameserver.data.xml.parser.NpcParser;
import jts.gameserver.data.xml.parser.ZoneParser;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.quest.Quest;
import jts.gameserver.model.quest.QuestState;
import jts.gameserver.scripts.Scripts;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.xmrpcserver.model.Message.MessageType;

import org.apache.commons.lang3.math.NumberUtils;


public class AdminService extends Base
{
	/**
	 * Перезагрузка указанного инстанса
	 * @param instance инстанс для перезагрузки
	 * @return {@code OK} если перезагрузка удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String reloadInstance(String instance)
	{
		try
		{
			switch(instance)
			{
				case "config":
					Config.load();
					break;
				case "html":
					HtmCache.getInstance().reload();
					break;
				case "item":
					ItemParser.getInstance().reload();
					break;
				case "multisell":
					MultiSellHolder.getInstance().reload();
					break;
				case "scripts":
					Scripts.getInstance().reload();
					break;
				case "npc":
					NpcParser.getInstance().reload();
					break;
				case "quest":
					for(Player p : GameObjectsStorage.getAllPlayersForIterate())
						reloadQuestStates(p);
					break;
				case "skill":
					SkillTable.getInstance().reload();
					break;
				case "zone":
					ZoneParser.getInstance().reload();
					break;
				case "primeshop":
					ProductHolder.getInstance().reload();
					break;
				case "access":
					try
					{
						Config.loadGMAccess();
						for(Player player : GameObjectsStorage.getAllPlayersForIterate())
							if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
								player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
							else
								player.setPlayerAccess(Config.gmlist.get(new Integer(0)));
					}
					catch(Exception e)
					{
						return String.valueOf(false);
					}
					break;
			}
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Выключение\рестарт сервера
	 * @param restart {@code true} рестартовать сервер после выключения, {@code false} если сервер нужно просто выключить
	 * @return {@code OK} если перезагрузка удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String restartServer(String restart)
	{
		try
		{
			Shutdown.getInstance().schedule(NumberUtils.toInt("XML-RPC", -1), Shutdown.RESTART);
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Отмена выключения\перезагрузки сервера
	 * @return {@code OK} если отмена удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String abortRestartServer()
	{
		try
		{
			Shutdown.getInstance().cancel();
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	private void reloadQuestStates(Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
			p.removeQuestState(qs.getQuest().getName());
		Quest.restoreQuestStates(p);
	}
}