package events.Inferno;

import jts.gameserver.Announcements;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.network.serverpackets.ExBR_BroadcastEventState;
import jts.gameserver.scripts.Functions;
import jts.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO В стадии разработки
public class Inferno extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(Inferno.class);
	private static boolean _active = false;
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			_log.info("Loaded Event: Inferno Event [state: activated]");
		}
		else
			_log.info("Loaded Event: Inferno Event [state: deactivated]");
	}
	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}

	private static boolean isActive()
	{
		return IsActive("Inferno");
	}
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("Inferno", true))
		{
			System.out.println("Event 'Inferno Event' started.");
			ExBR_BroadcastEventState es = new ExBR_BroadcastEventState(ExBR_BroadcastEventState.EVAS_INFERNO, 1);
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				p.sendPacket(es);
			Announcements.getInstance().announceByCustomMessage("scripts.events.Inferno.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Inferno Event' already started.");

		_active = true;

		show("admin/events/events.htm", player);
	}

	/**
	 * stops EVENT
	 */
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;
		if(SetActive("Inferno", false))
		{
			System.out.println("Event 'Inferno Event' stopped.");
			ExBR_BroadcastEventState es = new ExBR_BroadcastEventState(ExBR_BroadcastEventState.EVAS_INFERNO, 0);
			for(Player p : GameObjectsStorage.getAllPlayersForIterate())
				p.sendPacket(es);
			Announcements.getInstance().announceByCustomMessage("scripts.events.Inferno.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Inferno not started.");

		_active = false;

		show("admin/events/events.htm", player);
	}

	public void onPlayerEnter(Player player)
	{
		if(_active)
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Inferno.AnnounceEventStarted", null);
	}
}